package com.somshare.somshare.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerificationResponse {
    private final String message;

    public static VerificationResponse of(String message) {
        return new VerificationResponse(message);
    }
}
