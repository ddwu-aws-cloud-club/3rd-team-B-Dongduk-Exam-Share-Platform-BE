package com.somshare.somshare.dto;

import com.somshare.somshare.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private LoginUserDto user;

    public static LoginResponse of(String token, User user) {
        return LoginResponse.builder()
                .token(token)
                .user(LoginUserDto.from(user))
                .build();
    }
}
