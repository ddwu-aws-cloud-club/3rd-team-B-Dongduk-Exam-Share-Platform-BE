package com.somshare.somshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
    @NotBlank(message = "제목을 입력해주세요.")
    @Size(min = 3, max = 100, message = "제목은 3자 이상 100자 이하로 입력해주세요.")
    String title,

    @NotBlank(message = "과목명을 입력해주세요.")
    @Size(min = 2, max = 50, message = "과목명은 2자 이상 50자 이하로 입력해주세요.")
    String subject,

    @NotBlank(message = "교수명을 입력해주세요.")
    @Size(min = 2, max = 20, message = "교수명은 2자 이상 20자 이하로 입력해주세요.")
    String professor,

    @NotBlank(message = "전공을 선택해주세요.")
    String major
) {}
