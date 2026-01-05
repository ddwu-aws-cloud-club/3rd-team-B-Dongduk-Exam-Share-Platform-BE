package com.somshare.somshare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String email;
    private Integer points;
    private Boolean isVerified;

    public static LoginResponse of(String token, String email, Integer points, Boolean isVerified) {
        return LoginResponse.builder()
                .token(token)
                .email(email)
                .points(points)
                .isVerified(isVerified)
                .build();
    }
}
