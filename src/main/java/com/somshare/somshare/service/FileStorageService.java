package com.somshare.somshare.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

public interface FileStorageService {

    StoredFile storePdf(MultipartFile file) throws IOException;

    StoredFile storeImage(MultipartFile file) throws IOException;

    record StoredFile(
            String originalName,
            String storedName,
            String url,
            long size,
            String contentType,
            LocalDateTime createdAt
    ) {}
}
