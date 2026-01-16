package com.somshare.somshare.dto;

import com.somshare.somshare.domain.ExamPost;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MyUploadItemResponse {
    private Long id;
    private String title;
    private String subject;
    private String professor;
    private LocalDateTime uploadDate;   // createdAt 매핑
    private long downloadCount;
    private long earnedPoints;          // points 매핑

    public static MyUploadItemResponse from(ExamPost post) {
        return new MyUploadItemResponse(
                post.getId(),
                post.getTitle(),
                post.getSubject(),
                post.getProfessor(),
                post.getCreatedAt(),
                post.getDownloadCount(),
                post.getPoints()
        );
    }
}
