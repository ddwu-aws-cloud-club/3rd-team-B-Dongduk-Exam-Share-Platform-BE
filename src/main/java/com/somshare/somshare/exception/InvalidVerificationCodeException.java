package com.somshare.somshare.exception;

public class InvalidVerificationCodeException extends RuntimeException {
    public InvalidVerificationCodeException(String message) {
        super(message);
    }

    public InvalidVerificationCodeException() {
        super("인증 코드가 일치하지 않거나 만료되었습니다.");
    }
}
