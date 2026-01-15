package com.somshare.somshare.dto;

import java.time.LocalDateTime;

public record ExamPostSummaryResponse(
        Long id,
        String title,
        String subject,
        String professor,
        String major,
        String majorName,
        LocalDateTime uploadDate,
        String uploaderName,
        long points,
        long downloadCount
) {

    // ✅ DTO가 마스킹 책임을 가짐
    public static ExamPostSummaryResponse from(
            Long id,
            String title,
            String subject,
            String professor,
            String major,
            String majorName,
            LocalDateTime uploadDate,
            String realUploaderName,
            long points,
            long downloadCount
    ) {
        return new ExamPostSummaryResponse(
                id,
                title,
                subject,
                professor,
                major,
                majorName,
                uploadDate,
                maskUploader(realUploaderName),
                points,
                downloadCount
        );
    }

    private static String maskUploader(String name) {
        if (name == null || name.isBlank()) {
            return "익명";
        }
        return "익명";
        // 확장 예: name.charAt(0) + "**"
    }
}
