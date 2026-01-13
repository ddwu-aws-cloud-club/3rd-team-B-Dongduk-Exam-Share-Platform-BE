package com.somshare.somshare.service;

import com.somshare.somshare.domain.User;
import com.somshare.somshare.dto.UserMeResponse;
import com.somshare.somshare.exception.UserNotFoundException;
import com.somshare.somshare.repository.ExamPostRepository;
import com.somshare.somshare.repository.PointHistoryRepository;
import com.somshare.somshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final ExamPostRepository examPostRepository;
    private final PointHistoryRepository pointHistoryRepository;

    /**
     * 현재 로그인한 사용자의 상세 정보 조회
     * @param userId JWT 토큰에서 추출한 사용자 ID
     * @return UserMeResponse 사용자 상세 정보
     */
    public UserMeResponse getUserMe(Long userId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 통계 정보 조회
        Long totalUploads = examPostRepository.countByUploaderId(userId);
        Long totalDownloads = pointHistoryRepository.countDownloads(userId);
        Integer totalEarnedPoints = pointHistoryRepository.getTotalEarnedPoints(userId);
        Integer totalSpentPoints = pointHistoryRepository.getTotalSpentPoints(userId);

        return UserMeResponse.of(user, totalUploads, totalDownloads, totalEarnedPoints, totalSpentPoints);
    }
}
