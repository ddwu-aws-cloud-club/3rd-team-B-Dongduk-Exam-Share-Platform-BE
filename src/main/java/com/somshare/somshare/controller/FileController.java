package com.somshare.somshare.controller;

import com.somshare.somshare.dto.PresignedUrlRequest;
import com.somshare.somshare.dto.PresignedUrlResponse;
import com.somshare.somshare.dto.UploadResponse;
import com.somshare.somshare.service.FileStorageService;
import com.somshare.somshare.service.PresignedUrlService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService storageService;

    @Autowired(required = false)
    private PresignedUrlService presignedUrlService;

    public FileController(FileStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) throws Exception {
        var stored = storageService.storePdf(file);
        return ResponseEntity.ok(new UploadResponse(
                stored.originalName(),
                stored.storedName(),
                stored.url(),
                stored.size(),
                stored.contentType(),
                stored.createdAt()
        ));
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> generatePresignedUrl(
            @Valid @RequestBody PresignedUrlRequest request
    ) {
        if (presignedUrlService == null) {
            return ResponseEntity.status(501)
                    .body(null); // simple/local 프로파일에서는 지원하지 않음
        }

        PresignedUrlResponse response = presignedUrlService.generatePresignedUrl(request);
        return ResponseEntity.ok(response);
    }
}