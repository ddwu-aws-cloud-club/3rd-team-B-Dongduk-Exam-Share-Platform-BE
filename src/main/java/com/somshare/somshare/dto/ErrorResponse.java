package com.somshare.somshare.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final String message;
    private final int status;
    private final LocalDateTime timestamp;

    public static ErrorResponse of(String message, int status) {
        return new ErrorResponse(message, status, LocalDateTime.now());
    }
}
