package com.somshare.somshare.dto;

import jakarta.validation.constraints.NotBlank;

public record ExamPostUpdateRequest(
        @NotBlank String title,
        String content,
        String fileKey,
        String fileUrl
) {}
