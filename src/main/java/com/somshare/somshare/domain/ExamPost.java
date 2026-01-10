package com.somshare.somshare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class ExamPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    // 파일 메타데이터 (S3)
    @Column(length = 500)
    private String fileKey;

    @Column(length = 1000)
    private String fileUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // 생성자 (게시글 생성)
    public ExamPost(
            String title,
            String content,
            User uploader,
            Department department,
            String fileKey,
            String fileUrl
    ) {
        this.title = title;
        this.content = content;
        this.uploader = uploader;
        this.department = department;
        this.fileKey = fileKey;
        this.fileUrl = fileUrl;
    }

    // 제목/내용 수정
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // 파일만 교체
    public void updateFile(String fileKey, String fileUrl) {
        this.fileKey = fileKey;
        this.fileUrl = fileUrl;
    }

    // 제목/내용/파일 한 번에 수정 (선택적으로 쓰기)
    public void updateAll(String title, String content, String fileKey, String fileUrl) {
        this.title = title;
        this.content = content;
        this.fileKey = fileKey;
        this.fileUrl = fileUrl;
    }
}
