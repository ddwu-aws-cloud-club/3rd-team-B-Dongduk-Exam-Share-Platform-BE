package com.somshare.somshare.dto;

import com.somshare.somshare.domain.ExamPost;
import com.somshare.somshare.domain.Post;
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

    // Post 엔티티용 팩토리 메서드
    public static MyUploadItemResponse from(Post post, long downloadCount, long earnedPoints) {
        return new MyUploadItemResponse(
                post.getId(),
                post.getTitle(),
                post.getSubject(),
                post.getProfessor(),
                post.getUploadDate(),
                downloadCount,
                earnedPoints
        );
    }
}
