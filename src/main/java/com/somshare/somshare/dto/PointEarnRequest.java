package com.somshare.somshare.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PointEarnRequest {

    private String fileName;
    private String originalName;
    private Long fileSize;

    // 기존 필드
    private int points;
    private String description;
}