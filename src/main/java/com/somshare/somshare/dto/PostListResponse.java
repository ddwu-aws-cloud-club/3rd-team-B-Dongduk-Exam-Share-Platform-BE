package com.somshare.somshare.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record PostListResponse(
        List<PostSummary> content,
        long totalElements,
        int totalPages,
        int currentPage
) {

    @Builder
    public record PostSummary(
            Long id,
            String title,
            String subject,
            String professor,
            String major,
            String uploadDate,
            Long uploaderId,
            String uploaderNickname,
            int downloadCount,
            int points,
            long likeCount,
            long dislikeCount
    ) {
    }
}
