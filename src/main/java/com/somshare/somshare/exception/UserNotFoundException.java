package com.somshare.somshare.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException() {
        super("이메일 또는 비밀번호가 일치하지 않습니다.");
    }
}
