package com.somshare.somshare.dto;

import jakarta.validation.constraints.NotBlank;

public record ExamPostCreateRequest(
        @NotBlank String title,
        String content,
        String subject,
        String professor,
        String fileKey,
        String fileUrl
) {}
