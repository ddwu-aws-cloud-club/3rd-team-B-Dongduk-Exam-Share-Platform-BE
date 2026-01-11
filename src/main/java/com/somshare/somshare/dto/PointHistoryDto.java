package com.somshare.somshare.dto;

import com.somshare.somshare.domain.PointType;
import com.somshare.somshare.domain.PointHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "포인트 변동 내역 응답 DTO")
public class PointHistoryDto {
    private Long id;
    private int amount;
    private PointType type;
    private String description;
    private LocalDateTime createdAt;

    public static PointHistoryDto from(PointHistory entity) {
        return PointHistoryDto.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .type(entity.getType())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
