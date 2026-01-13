package com.somshare.somshare.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "포인트 사용 요청 DTO")
public class PointReduceRequest {
    @Schema(description = "사용 사유")
    @NotBlank
    private String description;

    // 추가 : 다운로드하려는 파일 ID
    private Long fileId;
}
