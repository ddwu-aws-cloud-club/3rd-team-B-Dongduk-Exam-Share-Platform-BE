package com.somshare.somshare.dto;

import java.time.LocalDateTime;

public record PresignedUrlResponse(
        String presignedUrl,
        String fileKey,
        String fileName,
        LocalDateTime expiresAt
) {
}
