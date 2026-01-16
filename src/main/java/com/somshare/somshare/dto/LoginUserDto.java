package com.somshare.somshare.dto;

import com.somshare.somshare.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginUserDto {

    private Long id;
    private String email;
    private String nickname;
    private String college;
    private String major;
    private Integer points;
    private String profileImage;
    private Boolean isVerified;

    public static LoginUserDto from(User user) {
        return LoginUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .college(user.getCollege())
                .major(user.getMajor())
                .points(user.getPoints())
                .profileImage(user.getProfileImageUrl())
                .isVerified(user.getIsVerified())
                .build();
    }
}
