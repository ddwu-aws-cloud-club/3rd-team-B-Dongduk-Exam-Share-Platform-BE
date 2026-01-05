package com.somshare.somshare.service;

import com.somshare.somshare.exception.FileSizeExceededException;
import com.somshare.somshare.exception.FileUploadException;
import com.somshare.somshare.exception.InvalidFileTypeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@Profile({"dev", "prod"})
public class S3StorageService implements FileStorageService {

    private final S3Client s3Client;
    private final String bucketName;
    private final String region;

    private static final Set<String> ALLOWED_PDF_MIME = Set.of("application/pdf");
    private static final Set<String> ALLOWED_IMAGE_MIME = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_PDF_SIZE = 20 * 1024 * 1024; // 20MB
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    public S3StorageService(
            S3Client s3Client,
            @Value("${aws.s3.bucket}") String bucketName,
            @Value("${aws.region}") String region
    ) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.region = region;
    }

    @Override
    public StoredFile storePdf(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("파일이 비어있습니다.");
        }

        String contentType = file.getContentType();
        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename()
        );

        // PDF 파일 검증
        boolean mimeOk = contentType != null && ALLOWED_PDF_MIME.contains(contentType);
        boolean extOk = originalName.toLowerCase().endsWith(".pdf");

        if (!mimeOk && !extOk) {
            throw new InvalidFileTypeException("PDF 파일만 업로드할 수 있습니다.");
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_PDF_SIZE) {
            throw new FileSizeExceededException("PDF 파일 크기는 20MB 이하여야 합니다.");
        }

        return uploadToS3(file, originalName, contentType, "pdfs");
    }

    @Override
    public StoredFile storeImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("파일이 비어있습니다.");
        }

        String contentType = file.getContentType();
        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename()
        );

        // 이미지 파일 검증
        boolean mimeOk = contentType != null && ALLOWED_IMAGE_MIME.contains(contentType);
        boolean extOk = originalName.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|webp)$");

        if (!mimeOk && !extOk) {
            throw new InvalidFileTypeException("이미지 파일만 업로드할 수 있습니다. (jpg, jpeg, png, gif, webp)");
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new FileSizeExceededException("이미지 크기는 5MB 이하여야 합니다.");
        }

        return uploadToS3(file, originalName, contentType, "images");
    }

    private StoredFile uploadToS3(
            MultipartFile file,
            String originalName,
            String contentType,
            String folder
    ) throws IOException {
        // 안전한 파일명 생성
        String safeOriginal = originalName.replaceAll("[\\\\/:*?\"<>|]", "_");
        String fileKey = folder + "/" + UUID.randomUUID() + "-" + safeOriginal;

        try {
            // S3에 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    //.acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            // S3 URL 생성
            String s3Url = String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucketName, region, fileKey);

            return new StoredFile(
                    originalName,
                    fileKey,
                    s3Url,
                    file.getSize(),
                    contentType,
                    LocalDateTime.now()
            );

        } catch (Exception e) {
            throw new FileUploadException("S3 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
