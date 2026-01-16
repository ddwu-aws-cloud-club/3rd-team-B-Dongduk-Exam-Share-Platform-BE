package com.somshare.somshare.dto;

import com.somshare.somshare.domain.Download;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MyDownloadItemResponse {
    private Long id;
    private Long postId;
    private String title;
    private String subject;
    private String professor;
    private LocalDateTime downloadDate;
    private Integer pointsDeducted;

    public static MyDownloadItemResponse from(Download download) {
        return new MyDownloadItemResponse(
                download.getId(),
                download.getPost().getId(),
                download.getPost().getTitle(),
                download.getPost().getSubject(),
                download.getPost().getProfessor(),
                download.getDownloadedAt(),
                download.getPointsDeducted()
        );
    }
}
