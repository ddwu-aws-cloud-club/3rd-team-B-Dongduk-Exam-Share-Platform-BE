package com.somshare.somshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class ExamPostCreateForm {

    @NotBlank
    private String title;

    private String content;

    private MultipartFile pdf;
}
