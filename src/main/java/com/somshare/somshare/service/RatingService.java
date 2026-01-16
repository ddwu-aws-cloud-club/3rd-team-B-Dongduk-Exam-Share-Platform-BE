package com.somshare.somshare.service;

import com.somshare.somshare.domain.Post;
import com.somshare.somshare.domain.Rating;
import com.somshare.somshare.domain.RatingType;
import com.somshare.somshare.domain.User;
import com.somshare.somshare.dto.RatingResponse;
import com.somshare.somshare.repository.DownloadRepository;
import com.somshare.somshare.repository.PostRepository;
import com.somshare.somshare.repository.RatingRepository;
import com.somshare.somshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingService {

    private final RatingRepository ratingRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final DownloadRepository downloadRepository;

    @Transactional
    public RatingResponse ratePost(Long postId, String ratingType, String userEmail) {
        // 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 다운로드 여부 확인 (다운로드한 사용자만 평가 가능)
        boolean hasDownloaded = downloadRepository.existsByUserIdAndPostId(user.getId(), postId);
        if (!hasDownloaded) {
            throw new IllegalStateException("다운로드 후에 평가할 수 있습니다.");
        }

        RatingType type = RatingType.valueOf(ratingType.toUpperCase());

        // 기존 평가 조회
        Optional<Rating> existingRating = ratingRepository.findByUserIdAndPostId(user.getId(), postId);

        if (existingRating.isPresent()) {
            Rating rating = existingRating.get();
            if (rating.getRatingType() == type) {
                // 같은 평가를 다시 누르면 취소
                ratingRepository.delete(rating);
                log.info("[RATING_CANCEL] userId={} postId={} type={}", user.getId(), postId, type);
            } else {
                // 다른 평가로 변경
                rating.updateRatingType(type);
                log.info("[RATING_CHANGE] userId={} postId={} type={}", user.getId(), postId, type);
            }
        } else {
            // 새 평가 생성
            Rating newRating = new Rating(user, post, type);
            ratingRepository.save(newRating);
            log.info("[RATING_CREATE] userId={} postId={} type={}", user.getId(), postId, type);
        }

        // 현재 좋아요/싫어요 수 조회
        long likeCount = ratingRepository.countByPostIdAndRatingType(postId, RatingType.LIKE);
        long dislikeCount = ratingRepository.countByPostIdAndRatingType(postId, RatingType.DISLIKE);

        // 현재 사용자의 평가 상태 조회
        String userRating = ratingRepository.findByUserIdAndPostId(user.getId(), postId)
                .map(r -> r.getRatingType().name().toLowerCase())
                .orElse(null);

        return new RatingResponse(likeCount, dislikeCount, userRating);
    }

    public RatingResponse getRatingCounts(Long postId) {
        long likeCount = ratingRepository.countByPostIdAndRatingType(postId, RatingType.LIKE);
        long dislikeCount = ratingRepository.countByPostIdAndRatingType(postId, RatingType.DISLIKE);
        return new RatingResponse(likeCount, dislikeCount, null);
    }

    public RatingResponse getRatingCountsWithUserRating(Long postId, String userEmail) {
        long likeCount = ratingRepository.countByPostIdAndRatingType(postId, RatingType.LIKE);
        long dislikeCount = ratingRepository.countByPostIdAndRatingType(postId, RatingType.DISLIKE);

        String userRating = null;
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                userRating = ratingRepository.findByUserIdAndPostId(user.getId(), postId)
                        .map(r -> r.getRatingType().name().toLowerCase())
                        .orElse(null);
            }
        }

        return new RatingResponse(likeCount, dislikeCount, userRating);
    }
}
