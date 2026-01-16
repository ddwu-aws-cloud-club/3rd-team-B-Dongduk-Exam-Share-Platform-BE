package com.somshare.somshare.dto;

import java.util.List;

public record ExamPostListResponse(
        List<ExamPostSummaryResponse> content,
        long totalElements,
        int totalPages,
        int currentPage,
        boolean hasNext,
        boolean hasPrevious
) {}
