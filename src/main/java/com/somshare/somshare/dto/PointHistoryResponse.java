package com.somshare.somshare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PointHistoryResponse {
    private List<PointHistoryDto> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private PointSummary summary;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PointSummary {
        private int totalEarned;
        private int totalSpent;
        private int currentBalance;
    }

}