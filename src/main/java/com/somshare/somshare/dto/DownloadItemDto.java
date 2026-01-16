package com.somshare.somshare.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DownloadItemDto {

    private Long id; // PointHistory ID
    private Long postId; // Post ID
    private String title;
    private String subject;
    private String professor;
    private LocalDateTime downloadDate;
    private Integer pointsDeducted;
    private String pdfUrl;

    public static DownloadItemDto of(Long historyId, Long postId, String title, String subject,
                                      String professor, LocalDateTime downloadDate,
                                      Integer pointsDeducted, String pdfUrl) {
        return DownloadItemDto.builder()
                .id(historyId)
                .postId(postId)
                .title(title)
                .subject(subject)
                .professor(professor)
                .downloadDate(downloadDate)
                .pointsDeducted(Math.abs(pointsDeducted)) // 음수를 양수로 변환
                .pdfUrl(pdfUrl)
                .build();
    }
}
