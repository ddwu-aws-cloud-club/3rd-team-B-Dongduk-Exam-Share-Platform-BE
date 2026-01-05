package com.somshare.somshare.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException() {
        super("이메일 또는 비밀번호가 일치하지 않습니다.");
    }
}
