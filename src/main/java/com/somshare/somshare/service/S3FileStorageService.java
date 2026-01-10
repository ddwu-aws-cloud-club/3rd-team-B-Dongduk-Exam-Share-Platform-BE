package com.somshare.somshare.service;

import com.somshare.somshare.config.AwsS3Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Slf4j
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
        long start = System.currentTimeMillis();

        String originalName = safeName(file == null ? null : file.getOriginalFilename());
        Long size = file == null ? null : file.getSize();
        String contentType = file == null ? null : file.getContentType();

        log.info("[UPLOAD_PDF_REQ] filename={} size={} contentType={}", originalName, size, contentType);

        try {
            if (file == null || file.isEmpty()) {
                log.warn("[UPLOAD_PDF_REJECT] reason=empty filename={}", originalName);
                throw new IllegalArgumentException("파일이 비어있습니다.");
            }

            String cleanedOriginal = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());

            boolean mimeOk = contentType != null && ALLOWED_PDF_MIME.contains(contentType);
            boolean extOk = cleanedOriginal.toLowerCase().endsWith(".pdf");

            if (!mimeOk && !extOk) {
                log.warn("[UPLOAD_PDF_REJECT] reason=type_not_allowed filename={} contentType={}",
                        safeName(cleanedOriginal), contentType);
                throw new IllegalArgumentException("PDF 파일만 업로드할 수 있습니다.");
            }

            StoredFile stored = uploadToS3(file, "pdfs/", contentType);

            log.info("[UPLOAD_PDF_OK] storedName={} size={} elapsedMs={}",
                    stored.storedName(), stored.size(), System.currentTimeMillis() - start);

            return stored;

        } catch (Exception e) {
            log.error("[UPLOAD_PDF_FAIL] filename={} elapsedMs={}", originalName, System.currentTimeMillis() - start, e);
            throw e;
        }
    }

    public StoredFile storeImage(MultipartFile file) throws IOException {
        long start = System.currentTimeMillis();

        String originalName = safeName(file == null ? null : file.getOriginalFilename());
        Long size = file == null ? null : file.getSize();
        String contentType = file == null ? null : file.getContentType();

        log.info("[UPLOAD_IMG_REQ] filename={} size={} contentType={}", originalName, size, contentType);

        try {
            if (file == null || file.isEmpty()) {
                log.warn("[UPLOAD_IMG_REJECT] reason=empty filename={}", originalName);
                throw new IllegalArgumentException("파일이 비어있습니다.");
            }

            String cleanedOriginal = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());

            boolean mimeOk = contentType != null && ALLOWED_IMAGE_MIME.contains(contentType);
            boolean extOk = cleanedOriginal.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|webp)$");

            if (!mimeOk && !extOk) {
                log.warn("[UPLOAD_IMG_REJECT] reason=type_not_allowed filename={} contentType={}",
                        safeName(cleanedOriginal), contentType);
                throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다. (jpg, jpeg, png, gif, webp)");
            }

            // 파일 크기 제한 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                log.warn("[UPLOAD_IMG_REJECT] reason=size_too_large filename={} size={}",
                        safeName(cleanedOriginal), file.getSize());
                throw new IllegalArgumentException("이미지 크기는 5MB 이하여야 합니다.");
            }

            StoredFile stored = uploadToS3(file, "profiles/", contentType);

            log.info("[UPLOAD_IMG_OK] storedName={} size={} elapsedMs={}",
                    stored.storedName(), stored.size(), System.currentTimeMillis() - start);

            return stored;

        } catch (Exception e) {
            log.error("[UPLOAD_IMG_FAIL] filename={} elapsedMs={}", originalName, System.currentTimeMillis() - start, e);
            throw e;
        }
    }

    private StoredFile uploadToS3(MultipartFile file, String folder, String contentType) throws IOException {
        long start = System.currentTimeMillis();

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String safeOriginal = originalName.replaceAll("[\\\\/:*?\"<>|]", "_");
        String storedName = folder + UUID.randomUUID() + "-" + safeOriginal;

        String bucketName = awsS3Config.getBucketName();

        log.info("[S3_PUT_START] bucket={} key={} filename={} size={} contentType={}",
                bucketName,
                storedName,
                safeName(originalName),
                file.getSize(),
                contentType
        );

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storedName)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            // S3 URL 생성
            String url = String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s", bucketName, storedName);

            log.info("[S3_PUT_OK] key={} elapsedMs={}", storedName, System.currentTimeMillis() - start);

            return new StoredFile(originalName, storedName, url, file.getSize());

        } catch (Exception e) {
            log.error("[S3_PUT_FAIL] key={} elapsedMs={}", storedName, System.currentTimeMillis() - start, e);
            throw e;
        }
    }

    private String safeName(String name) {
        if (name == null) return "null";
        // 로그 깨는 문자 제거
        return name.replaceAll("[\\r\\n\\t]", "_");
    }

    public record StoredFile(String originalName, String storedName, String url, long size) {}
}
