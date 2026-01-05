package com.somshare.somshare.service;

import com.somshare.somshare.dto.PresignedUrlRequest;
import com.somshare.somshare.dto.PresignedUrlResponse;
import com.somshare.somshare.exception.FileSizeExceededException;
import com.somshare.somshare.exception.InvalidFileTypeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@Profile({"dev", "prod"})
public class PresignedUrlService {

    private final S3Presigner s3Presigner;
    private final String bucketName;

    private static final Set<String> ALLOWED_PDF_MIME = Set.of("application/pdf");
    private static final long MAX_PDF_SIZE = 20 * 1024 * 1024; // 20MB
    private static final Duration URL_EXPIRATION = Duration.ofMinutes(15); // 15분

    public PresignedUrlService(
            S3Presigner s3Presigner,
            @Value("${aws.s3.bucket}") String bucketName
    ) {
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
    }

    public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request) {
        // 파일 타입 검증
        if (!ALLOWED_PDF_MIME.contains(request.contentType())) {
            throw new InvalidFileTypeException("PDF 파일만 업로드할 수 있습니다.");
        }

        // 파일 크기 검증
        if (request.fileSize() > MAX_PDF_SIZE) {
            throw new FileSizeExceededException("PDF 파일 크기는 20MB 이하여야 합니다.");
        }

        // 파일명 검증 및 정리
        String fileName = StringUtils.cleanPath(request.fileName());
        if (!fileName.toLowerCase().endsWith(".pdf")) {
            throw new InvalidFileTypeException("PDF 파일만 업로드할 수 있습니다.");
        }

        // S3 객체 키 생성 (UUID로 충돌 방지)
        String safeFileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        String fileKey = "pdfs/" + UUID.randomUUID() + "-" + safeFileName;

        // Presigned URL 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .contentType(request.contentType())
                .contentLength(request.fileSize())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(URL_EXPIRATION)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new PresignedUrlResponse(
                presignedRequest.url().toString(),
                fileKey,
                fileName,
                LocalDateTime.now().plus(URL_EXPIRATION)
        );
    }
}
