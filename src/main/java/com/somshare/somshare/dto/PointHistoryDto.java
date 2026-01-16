package com.somshare.somshare.dto;

import com.somshare.somshare.domain.PointType;
import com.somshare.somshare.domain.PointHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collector;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "포인트 변동 내역 응답 DTO")
public class PointHistoryDto {
    private Long id;
    private int amount;
    private PointType type;
    private String description;
    private LocalDateTime createdAt;
    private int balanceAfter;       //거래 후 잔액 추가

    public static PointHistoryDto from(PointHistory entity) {
        return PointHistoryDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .balanceAfter(entity.getBalanceAfter())
                .build();
    }
}
