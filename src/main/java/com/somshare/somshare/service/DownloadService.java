package com.somshare.somshare.service;

import com.somshare.somshare.domain.*;
import com.somshare.somshare.dto.DownloadResponse;
import com.somshare.somshare.exception.AlreadyDownloadedException;
import com.somshare.somshare.exception.InsufficientPointsException;
import com.somshare.somshare.exception.PostNotFoundException;
import com.somshare.somshare.exception.UserNotFoundException;
import com.somshare.somshare.repository.DownloadRepository;
import com.somshare.somshare.repository.PointHistoryRepository;
import com.somshare.somshare.repository.PostRepository;
import com.somshare.somshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DownloadService {

    private static final int DOWNLOAD_COST = 50;

    private final DownloadRepository downloadRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final S3DownloadService s3DownloadService;

    public DownloadResponse downloadPost(Long postId, String username) {
        log.info("[DOWNLOAD_REQ] postId={} username={}", postId, username);

        // 1. 사용자 조회
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 2. 게시물 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시물을 찾을 수 없습니다."));

        String fileName = post.getTitle() + ".pdf";
        String pdfKey = post.getPdfKey();

        // 3. 본인 업로드 족보인지 확인 (무료 다운로드)
        if (post.getUploader().getId().equals(user.getId())) {
            log.info("[DOWNLOAD_OWN] postId={} username={}", postId, username);
            String presignedUrl = s3DownloadService.generatePresignedGetUrl(pdfKey);
            return DownloadResponse.builder()
                    .pdfUrl(presignedUrl)
                    .fileName(fileName)
                    .pointsDeducted(0)
                    .remainingPoints(user.getPoints())
                    .message("본인이 업로드한 족보입니다. 무료로 다운로드됩니다.")
                    .build();
        }

        // 4. 기존 다운로드 이력 확인 (중복 차감 방지)
        if (downloadRepository.existsByUserIdAndPostId(user.getId(), postId)) {
            log.info("[DOWNLOAD_ALREADY] postId={} username={}", postId, username);
            String presignedUrl = s3DownloadService.generatePresignedGetUrl(pdfKey);
            throw new AlreadyDownloadedException(
                    "이미 다운로드한 족보입니다. 포인트 차감 없이 다운로드됩니다.",
                    presignedUrl,
                    fileName
            );
        }

        // 5. 포인트 충분 여부 확인
        if (user.getPoints() < DOWNLOAD_COST) {
            log.warn("[DOWNLOAD_INSUFFICIENT_POINTS] postId={} username={} points={}",
                    postId, username, user.getPoints());
            throw new InsufficientPointsException(
                    String.format("포인트가 부족합니다. (현재: %dP, 필요: %dP)", user.getPoints(), DOWNLOAD_COST)
            );
        }

        // 6. 트랜잭션 처리
        // 6-1. 포인트 차감
        user.deductPoints(DOWNLOAD_COST);

        // 6-2. 다운로드 히스토리 저장
        Download download = Download.builder()
                .user(user)
                .post(post)
                .pointsDeducted(DOWNLOAD_COST)
                .build();
        downloadRepository.save(download);

        // 6-3. 포인트 히스토리 기록
        PointHistory pointHistory = PointHistory.builder()
                .user(user)
                .fileId(postId)
                .amount(-DOWNLOAD_COST)
                .type(PointType.DOWNLOAD)
                .description("족보 다운로드: " + post.getTitle())
                .balanceAfter(user.getPoints())
                .build();
        pointHistoryRepository.save(pointHistory);

        // 7. Presigned URL 생성 및 반환
        String presignedUrl = s3DownloadService.generatePresignedGetUrl(pdfKey);

        log.info("[DOWNLOAD_OK] postId={} username={} pointsDeducted={} remainingPoints={}",
                postId, username, DOWNLOAD_COST, user.getPoints());

        return DownloadResponse.builder()
                .pdfUrl(presignedUrl)
                .fileName(fileName)
                .pointsDeducted(DOWNLOAD_COST)
                .remainingPoints(user.getPoints())
                .message(String.format("다운로드가 완료되었습니다. %dP가 차감되었습니다.", DOWNLOAD_COST))
                .build();
    }
}
