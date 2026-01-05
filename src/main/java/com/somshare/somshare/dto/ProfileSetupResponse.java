package com.somshare.somshare.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileSetupResponse {
    private String message;

    public static ProfileSetupResponse of(String message) {
        return new ProfileSetupResponse(message);
    }
}
