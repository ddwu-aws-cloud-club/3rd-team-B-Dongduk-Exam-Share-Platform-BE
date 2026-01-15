package com.somshare.somshare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "exam_post")
public class ExamPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    // ✅ 추가
    @Column(length = 100)
    private String subject;

    // ✅ 추가
    @Column(length = 100)
    private String professor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    private String fileKey;
    private String fileUrl;

    // ✅ 추가
    @Column(nullable = false)
    private long points = 0L;

    // ✅ 추가
    @Column(nullable = false)
    private long downloadCount = 0L;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public ExamPost(
            String title,
            String content,
            String subject,
            String professor,
            User uploader,
            Department department,
            String fileKey,
            String fileUrl
    ) {
        this.title = title;
        this.content = content;
        this.subject = subject;
        this.professor = professor;
        this.uploader = uploader;
        this.department = department;
        this.fileKey = fileKey;
        this.fileUrl = fileUrl;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String title, String content, String subject, String professor) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (subject != null) this.subject = subject;
        if (professor != null) this.professor = professor;
    }

    public void updateFile(String fileKey, String fileUrl) {
        if (fileKey != null) this.fileKey = fileKey;
        if (fileUrl != null) this.fileUrl = fileUrl;
    }

    public void incrementDownloadCount() {
        this.downloadCount += 1;
    }

    public void addPoints(long delta) {
        this.points += delta;
        if (this.points < 0) this.points = 0;
    }
}