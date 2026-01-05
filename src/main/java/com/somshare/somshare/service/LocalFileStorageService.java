package com.somshare.somshare.service;

import com.somshare.somshare.exception.FileSizeExceededException;
import com.somshare.somshare.exception.FileUploadException;
import com.somshare.somshare.exception.InvalidFileTypeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@Profile({"local", "simple"})
public class LocalFileStorageService implements FileStorageService {

    private final Path uploadDir;
    private static final Set<String> ALLOWED_PDF_MIME = Set.of("application/pdf");
    private static final Set<String> ALLOWED_IMAGE_MIME = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_PDF_SIZE = 20 * 1024 * 1024; // 20MB
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    public LocalFileStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override

    public StoredFile storePdf(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("파일이 비어있습니다.");
        }

        String contentType = file.getContentType();
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());

        boolean mimeOk = contentType != null && ALLOWED_PDF_MIME.contains(contentType);
        boolean extOk = originalName.toLowerCase().endsWith(".pdf");

        if (!mimeOk && !extOk) {
            throw new InvalidFileTypeException("PDF 파일만 업로드할 수 있습니다.");
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_PDF_SIZE) {
            throw new FileSizeExceededException("PDF 파일 크기는 20MB 이하여야 합니다.");
        }

        Files.createDirectories(uploadDir);

        String safeOriginal = originalName.replaceAll("[\\\\/:*?\"<>|]", "_");
        String storedName = UUID.randomUUID() + "-" + safeOriginal;

        Path target = uploadDir.resolve(storedName).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new FileUploadException("잘못된 파일 경로입니다.");
        }

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String url = "/uploads/" + storedName;

        return new StoredFile(
                originalName,
                storedName,
                url,
                file.getSize(),
                contentType != null ? contentType : "application/pdf",
                LocalDateTime.now()
        );
    }

    @Override

    public StoredFile storeImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("파일이 비어있습니다.");
        }

        String contentType = file.getContentType();
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());

        boolean mimeOk = contentType != null && ALLOWED_IMAGE_MIME.contains(contentType);
        boolean extOk = originalName.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|webp)$");

        if (!mimeOk && !extOk) {
            throw new InvalidFileTypeException("이미지 파일만 업로드할 수 있습니다. (jpg, jpeg, png, gif, webp)");
        }

        // 파일 크기 제한 (5MB)
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new FileSizeExceededException("이미지 크기는 5MB 이하여야 합니다.");
        }

        Files.createDirectories(uploadDir);

        String safeOriginal = originalName.replaceAll("[\\\\/:*?\"<>|]", "_");
        String storedName = UUID.randomUUID() + "-" + safeOriginal;

        Path target = uploadDir.resolve(storedName).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new FileUploadException("잘못된 파일 경로입니다.");
        }

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String url = "/uploads/" + storedName;

        return new StoredFile(
                originalName,
                storedName,
                url,
                file.getSize(),
                contentType != null ? contentType : "application/octet-stream",
                LocalDateTime.now()
        );
    }
}
