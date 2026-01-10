package com.somshare.somshare.dto;

import jakarta.validation.constraints.NotBlank;

public record ExamPostCreateRequest(
        @NotBlank String title,
        String content,
        String fileKey,   // 업로드 결과 storedName 같은 값
        String fileUrl    // 업로드 결과 url
) {}
