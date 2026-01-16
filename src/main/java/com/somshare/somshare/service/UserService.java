package com.somshare.somshare.service;

import com.somshare.somshare.domain.Download;
import com.somshare.somshare.domain.User;
import com.somshare.somshare.dto.MyDownloadItemResponse;
import com.somshare.somshare.dto.MyDownloadsPageResponse;
import com.somshare.somshare.dto.UserMeResponse;
import com.somshare.somshare.exception.UserNotFoundException;
import com.somshare.somshare.repository.DownloadRepository;
import com.somshare.somshare.repository.PointHistoryRepository;
import com.somshare.somshare.repository.PostRepository;
import com.somshare.somshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final DownloadRepository downloadRepository;

    /**
     * 현재 로그인한 사용자의 상세 정보 조회
     * @param userId JWT 토큰에서 추출한 사용자 ID
     * @return UserMeResponse 사용자 상세 정보
     */
    public UserMeResponse getUserMe(Long userId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 통계 정보 조회 (Post 테이블에서 업로드 수 조회)
        Long totalUploads = postRepository.countByUploaderId(userId);
        Long totalDownloads = downloadRepository.countByUserId(userId);
        Integer totalEarnedPoints = pointHistoryRepository.getTotalEarnedPoints(userId);
        Integer totalSpentPoints = pointHistoryRepository.getTotalSpentPoints(userId);

        return UserMeResponse.of(user, totalUploads, totalDownloads, totalEarnedPoints, totalSpentPoints);
    }

    /**
     * 사용자가 다운로드한 게시글 ID 목록 조회
     * @param userId 사용자 ID
     * @return 다운로드한 게시글 ID 목록
     */
    public List<Long> getDownloadedPostIds(Long userId) {
        return downloadRepository.findPostIdsByUserId(userId);
    }

    /**
     * 사용자의 다운로드 내역 조회 (페이징)
     * @param userId 사용자 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 다운로드 내역 페이지 응답
     */
    public MyDownloadsPageResponse getMyDownloads(Long userId, int page, int size) {
        Page<Download> downloadPage = downloadRepository.findByUserIdOrderByDownloadedAtDesc(
                userId, PageRequest.of(page, size));

        Page<MyDownloadItemResponse> responsePage = downloadPage.map(MyDownloadItemResponse::from);
        return MyDownloadsPageResponse.from(responsePage);
    }
}
