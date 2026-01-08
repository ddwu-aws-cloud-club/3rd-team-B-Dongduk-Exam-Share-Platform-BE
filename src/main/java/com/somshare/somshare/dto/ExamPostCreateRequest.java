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

    @NotNull
    private Long uploaderId; // 임시: 나중엔 토큰에서 꺼내기

    // ✅ 추가: 컨트롤러에서 new ExamPostCreateRequest(...) 가능하게
    public ExamPostCreateRequest(String title, String content, Long uploaderId) {
        this.title = title;
        this.content = content;
        this.uploaderId = uploaderId;
    }
}
