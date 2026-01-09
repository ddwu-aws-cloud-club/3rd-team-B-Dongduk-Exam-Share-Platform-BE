package com.somshare.somshare.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PointHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Long fileId;

    private int amount;

    @Enumerated(EnumType.STRING)
    private PointType type;

    private String description;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public PointHistory(User user, Long fileId, int amount, PointType type, String description) {
        this.user = user;
        this.fileId = fileId;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }
}