package com.somshare.somshare.dto;

import com.somshare.somshare.domain.Post;

import java.time.LocalDateTime;

public record PostUploadResponse(
        Long id,
        String title,
        String subject,
        String professor,
        String major,
        String pdfUrl,
        LocalDateTime uploadDate,
        int earnedPoints,
        String message
) {
    public static PostUploadResponse from(Post post, int earnedPoints, String message) {
        return new PostUploadResponse(
                post.getId(),
                post.getTitle(),
                post.getSubject(),
                post.getProfessor(),
                post.getMajor(),
                post.getPdfUrl(),
                post.getUploadDate(),
                earnedPoints,
                message
        );
    }
}
