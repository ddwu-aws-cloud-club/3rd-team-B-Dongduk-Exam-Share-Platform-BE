package com.somshare.somshare.dto;

import lombok.Builder;

@Builder
public record AlreadyDownloadedResponse(
        String message,
        Integer status,
        String pdfUrl,
        String fileName
) {
}
