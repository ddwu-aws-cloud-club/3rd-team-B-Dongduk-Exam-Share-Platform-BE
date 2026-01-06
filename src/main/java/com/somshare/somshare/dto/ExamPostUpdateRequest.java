package com.somshare.somshare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExamPostUpdateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;
}
