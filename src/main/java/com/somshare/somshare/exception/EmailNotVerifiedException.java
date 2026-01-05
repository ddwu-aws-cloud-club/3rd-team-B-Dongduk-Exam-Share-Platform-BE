package com.somshare.somshare.exception;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException(String message) {
        super(message);
    }

    public EmailNotVerifiedException() {
        super("이메일 인증이 완료되지 않았습니다. 인증 후 다시 시도해주세요.");
    }
}
