package com.somshare.somshare.dto;

import lombok.Builder;

@Builder
public record DownloadResponse(
        String pdfUrl,
        String fileName,
        Integer pointsDeducted,
        Integer remainingPoints,
        String message
) {
}
