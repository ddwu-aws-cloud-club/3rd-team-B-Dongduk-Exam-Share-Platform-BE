package com.somshare.somshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExamPostCreateRequest {

    @NotBlank
    private String title;

    private String content;

    private String fileUrl;

    @NotNull
    private Long uploaderId; // 임시: 나중엔 토큰에서 꺼내기
}

