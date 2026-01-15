package com.somshare.somshare.service;

import com.somshare.somshare.domain.PointHistory;
import com.somshare.somshare.domain.PointType;
import com.somshare.somshare.domain.User;
import com.somshare.somshare.dto.PointHistoryDto;
import com.somshare.somshare.dto.PointHistoryResponse;
import com.somshare.somshare.repository.PointHistoryRepository;
import com.somshare.somshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final UserRepository userRepository;
    private final PointHistoryRepository historyRepository;

    // 잔액 조회
    @Transactional(readOnly = true)
    public int getBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다."));
                return user.getPoints();
    }

    // 적립
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public void completeUploadAndEarnPoints(Long userId, String s3Key, String originalName, Long fileSize, String description) {

        // 유저 조회
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 없습니다."));

        // S3에 진짜 파일이 올라갔는지 확인
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.headObject(headRequest);

        } catch (NoSuchKeyException e) {
            throw new IllegalArgumentException("S3에 파일이 존재하지 않습니다. 업로드를 먼저 해주세요.");
        }

        // 포인트 적립 (임시: 100으로 설정)
        int earnedAmount = 100;
        user.addPoints(earnedAmount);

        // 포인트 내역 저장
        historyRepository.save(PointHistory.builder()
                .user(user)
                .amount(earnedAmount)
                .type(PointType.UPLOAD)
                .description("자료 업로드 보상: " + (description != null ? description : originalName))
                .build());
    }

    // 사용
    private final S3DownloadService s3Service;

    public String reducePoints(Long userId, Long fileId, String description) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다."));

        String fileKey = "files/" + fileId + ".pdf";
        int reduce_amount = 50;

        // 중복 구매 확인
        boolean alreadyPurchased = historyRepository.existsByUserIdAndFileIdAndType(
                userId, fileId, PointType.DOWNLOAD
        );

        if (alreadyPurchased) {
            return s3Service.generatePresignedGetUrl(fileKey);
        }

        // 잔액 체크 및 차감
        if (user.getPoints() < reduce_amount) {
            throw new IllegalArgumentException("포인트 부족");
        }
        user.deductPoints(reduce_amount);

        // 내역 저장
        historyRepository.save(PointHistory.builder()
                .user(user)
                .amount(-reduce_amount)
                .fileId(fileId)
                .type(PointType.DOWNLOAD)
                .description(description)
                .balanceAfter(user.getPoints())
                .build());

        //  URL 반환
        return s3Service.generatePresignedGetUrl(fileKey);
    }

    // 내역 조회
    @Transactional(readOnly = true)
    public PointHistoryResponse getHistory(Long userId, String typeStr, Pageable pageable){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다."));

        // 타입 필터링 처리 (ALL -> null로 넘김 -> 전체 조회)
        PointType type = (typeStr == null || typeStr.equals("ALL")) ? null : PointType.valueOf(typeStr);

        Page<PointHistory> historyPage = historyRepository.findAllByUserIdAndType(userId, type, pageable);

        // 요약 통계 계산
        int totalEarned = historyRepository.sumEarnedPoints(userId);
        int totalSpent = Math.abs(historyRepository.sumSpentPoints(userId));
        int currentBalance = user.getPoints();

        //Response 조립
        return PointHistoryResponse.builder()
                .content(historyPage.getContent().stream().map(PointHistoryDto::from).collect(Collectors.toList()))
                .totalElements(historyPage.getTotalElements())
                .totalPages(historyPage.getTotalPages())
                .currentPage(historyPage.getNumber())
                .summary(new PointHistoryResponse.PointSummary(totalEarned, totalSpent, currentBalance))
                .build();
    }
}
