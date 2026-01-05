package com.somshare.somshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSetupRequest {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 3, max = 10, message = "닉네임은 3자 이상 10자 이하로 입력해주세요.")
    private String nickname;

    @NotBlank(message = "소속 대학은 필수입니다.")
    private String college;

    @NotBlank(message = "전공은 필수입니다.")
    private String major;

    private MultipartFile profileImage;
}
