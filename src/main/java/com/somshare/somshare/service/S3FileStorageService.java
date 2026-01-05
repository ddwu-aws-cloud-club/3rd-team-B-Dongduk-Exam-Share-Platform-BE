package com.somshare.somshare.service;

import com.somshare.somshare.config.AwsS3Config;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3FileStorageService {

    private final S3Client s3Client;
    private final AwsS3Config awsS3Config;

    private static final Set<String> ALLOWED_PDF_MIME = Set.of("application/pdf");
    private static final Set<String> ALLOWED_IMAGE_MIME = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    public StoredFile storePdf(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String contentType = file.getContentType();
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());

        boolean mimeOk = contentType != null && ALLOWED_PDF_MIME.contains(contentType);
        boolean extOk = originalName.toLowerCase().endsWith(".pdf");

        if (!mimeOk && !extOk) {
            throw new IllegalArgumentException("PDF 파일만 업로드할 수 있습니다.");
        }

        return uploadToS3(file, "pdfs/", contentType);
    }

    public StoredFile storeImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String contentType = file.getContentType();
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());

        boolean mimeOk = contentType != null && ALLOWED_IMAGE_MIME.contains(contentType);
        boolean extOk = originalName.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|webp)$");

        if (!mimeOk && !extOk) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다. (jpg, jpeg, png, gif, webp)");
        }

        // 파일 크기 제한 (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("이미지 크기는 5MB 이하여야 합니다.");
        }

        return uploadToS3(file, "profiles/", contentType);
    }

    private StoredFile uploadToS3(MultipartFile file, String folder, String contentType) throws IOException {
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String safeOriginal = originalName.replaceAll("[\\\\/:*?\"<>|]", "_");
        String storedName = folder + UUID.randomUUID() + "-" + safeOriginal;

        String bucketName = awsS3Config.getBucketName();

        // S3에 업로드
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(storedName)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // S3 URL 생성
        String url = String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s", bucketName, storedName);

        return new StoredFile(originalName, storedName, url, file.getSize());
    }

    public record StoredFile(String originalName, String storedName, String url, long size) {}
}
