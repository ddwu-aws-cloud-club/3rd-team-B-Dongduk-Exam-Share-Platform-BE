package com.somshare.somshare.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }

    public DuplicateEmailException() {
        super("이미 사용 중인 이메일입니다.");
    }
}
