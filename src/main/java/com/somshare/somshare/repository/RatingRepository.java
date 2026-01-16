package com.somshare.somshare.repository;

import com.somshare.somshare.domain.Rating;
import com.somshare.somshare.domain.RatingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByUserIdAndPostId(Long userId, Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    long countByPostIdAndRatingType(Long postId, RatingType ratingType);

    void deleteByUserIdAndPostId(Long userId, Long postId);
}
