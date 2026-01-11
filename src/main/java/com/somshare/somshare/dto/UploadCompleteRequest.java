package com.somshare.somshare.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UploadCompleteRequest {
    private String fileName;    // 저장된 파일명 (S3 Key)
    private String originalName;
    private Long fileSize;
    private String description;
}