package com.somshare.somshare.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ratings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "post_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RatingType ratingType;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public Rating(User user, Post post, RatingType ratingType) {
        this.user = user;
        this.post = post;
        this.ratingType = ratingType;
    }

    public void updateRatingType(RatingType ratingType) {
        this.ratingType = ratingType;
    }
}
