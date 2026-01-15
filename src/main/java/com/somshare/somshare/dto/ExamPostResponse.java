package com.somshare.somshare.dto;

import java.time.LocalDateTime;

public record ExamPostResponse(
        Long id,
        String title,
        String content,
        String subject,
        String professor,
        Long uploaderId,
        Long departmentId,
        String fileKey,
        String fileUrl,
        long points,
        long downloadCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
