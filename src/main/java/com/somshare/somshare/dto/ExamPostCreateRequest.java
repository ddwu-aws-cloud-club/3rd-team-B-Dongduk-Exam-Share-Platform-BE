package com.somshare.somshare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExamPostCreateRequest {

    @NotBlank
    private String title;

    private String content;

    public ExamPostCreateRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
