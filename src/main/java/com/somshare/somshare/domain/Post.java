package com.somshare.somshare.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 50)
    private String subject;

    @Column(nullable = false, length = 20)
    private String professor;

    @Column(nullable = false, length = 50)
    private String major;

    @Column(nullable = false, length = 1000)
    private String pdfUrl;

    @Column(length = 500)
    private String pdfKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime uploadDate;

    public Post(String title, String subject, String professor, String major,
                String pdfUrl, String pdfKey, User uploader) {
        this.title = title;
        this.subject = subject;
        this.professor = professor;
        this.major = major;
        this.pdfUrl = pdfUrl;
        this.pdfKey = pdfKey;
        this.uploader = uploader;
    }

    public void update(String title, String subject, String professor, String major) {
        this.title = title;
        this.subject = subject;
        this.professor = professor;
        this.major = major;
    }
}
