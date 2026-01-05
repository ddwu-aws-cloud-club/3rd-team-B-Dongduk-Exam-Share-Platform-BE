package com.somshare.somshare.dto;

import java.time.LocalDateTime;

public record UploadResponse(
        String originalName,
        String storedName,
        String url,
        long size
) {}