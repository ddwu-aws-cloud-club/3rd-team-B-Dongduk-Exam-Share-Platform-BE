package com.somshare.somshare.exception;

public class EmailNotVerifiedBeforeSignupException extends RuntimeException {
    public EmailNotVerifiedBeforeSignupException(String message) {
        super(message);
    }

    public EmailNotVerifiedBeforeSignupException() {
        super("이메일 인증을 먼저 완료해주세요.");
    }
}
