package com.somshare.somshare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// ExamPost.java
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

    // ✅ 추가
    private String fileKey;

    private String fileUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ✅ 생성자 변경 (fileKey 추가)
    public ExamPost(String title, String content, User uploader, Department department, String fileKey, String fileUrl) {
        this.title = title;
        this.content = content;
        this.uploader = uploader;
        this.department = department;
        this.fileKey = fileKey;
        this.fileUrl = fileUrl;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // ✅ 파일 교체용
    public void updateFile(String fileKey, String fileUrl) {
        this.fileKey = fileKey;
        this.fileUrl = fileUrl;
    }
}
