package com.somshare.somshare.service;

import com.somshare.somshare.domain.Post;
import com.somshare.somshare.domain.PointHistory;
import com.somshare.somshare.domain.User;
import com.somshare.somshare.dto.DownloadHistoryResponse;
import com.somshare.somshare.dto.DownloadItemDto;
import com.somshare.somshare.dto.UserMeResponse;
import com.somshare.somshare.exception.UserNotFoundException;
import com.somshare.somshare.repository.ExamPostRepository;
import com.somshare.somshare.repository.PointHistoryRepository;
import com.somshare.somshare.repository.PostRepository;
import com.somshare.somshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final ExamPostRepository examPostRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PostRepository postRepository;

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

    /**
     * 현재 로그인한 사용자의 다운로드 목록 조회
     * @param userId JWT 토큰에서 추출한 사용자 ID
     * @param pageable 페이징 정보
     * @return DownloadHistoryResponse 다운로드 목록
     */
    public DownloadHistoryResponse getMyDownloads(Long userId, Pageable pageable) {
        // 다운로드 내역 조회 (PointHistory - type=REDUCE)
        Page<PointHistory> downloadHistory = pointHistoryRepository.findDownloadsByUserId(userId, pageable);

        // PointHistory -> DownloadItemDto 변환
        Page<DownloadItemDto> downloadItems = downloadHistory.map(history -> {
            // Post 조회 (fileId = postId)
            Post post = postRepository.findById(history.getFileId())
                    .orElse(null); // Post가 삭제된 경우 null

            if (post == null) {
                // Post가 없는 경우 기본값으로 처리
                return DownloadItemDto.of(
                        history.getId(),
                        history.getFileId(),
                        "삭제된 게시글",
                        "-",
                        "-",
                        history.getCreatedAt(),
                        history.getAmount(),
                        null
                );
            }

            return DownloadItemDto.of(
                    history.getId(),
                    post.getId(),
                    post.getTitle(),
                    post.getSubject(),
                    post.getProfessor(),
                    history.getCreatedAt(),
                    history.getAmount(),
                    post.getPdfUrl()
            );
        });

        return DownloadHistoryResponse.from(downloadItems);
    }
}
