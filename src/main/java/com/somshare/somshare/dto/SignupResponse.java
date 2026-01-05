package com.somshare.somshare.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponse {
    private final String email;
    private final String message;

    public static SignupResponse of(String email, String message) {
        return new SignupResponse(email, message);
    }

    public static SignupResponse of(String email) {
        return new SignupResponse(email, "회원가입이 완료되었습니다. 이메일 인증 후 로그인해주세요.");
    }
}
