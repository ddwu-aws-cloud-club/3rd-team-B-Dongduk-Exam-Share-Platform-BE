package com.somshare.somshare.dto;

import com.somshare.somshare.domain.ExamPost;

import java.time.LocalDateTime;

public record ExamPostResponse(
        Long id,
        String title,
        String content,
        Long uploaderId,
        Long departmentId,
        String fileUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ExamPostResponse from(ExamPost post) {
        return new ExamPostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUploader().getId(),
                post.getDepartment().getId(),
                post.getFileUrl(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
