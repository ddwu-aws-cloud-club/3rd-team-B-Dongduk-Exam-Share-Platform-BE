package com.somshare.somshare.service;

import com.somshare.somshare.domain.PointHistory;
import com.somshare.somshare.domain.PointType;
import com.somshare.somshare.domain.User;
import com.somshare.somshare.dto.PointHistoryDto;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final UserRepository userRepository;
    private final PointHistoryRepository historyRepository;

    // 잔액 조회
    public int getBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다."));
                return user.getPoints();
    }

    // 적립
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Transactional
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
                .type(PointType.EARN)
                .description("자료 업로드 보상: " + (description != null ? description : originalName))
                .build());
    }

    // 사용
    private final S3DownloadService s3Service;

    public String reducePoints(Long userId, Long fileId, int amount, String description) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        String fileKey = "files/" + fileId + ".pdf";

        // 중복 구매 확인
        boolean alreadyPurchased = historyRepository.existsByUserIdAndFileIdAndType(
                userId, fileId, PointType.REDUCE
        );

        if (alreadyPurchased) {
            return s3Service.generatePresignedGetUrl(fileKey);
        }

        // 잔액 체크 및 차감
        if (user.getPoints() < amount) {
            throw new IllegalArgumentException("포인트 부족");
        }
        user.deductPoints(amount);

        // 내역 저장
        historyRepository.save(PointHistory.builder()
                .user(user)
                .fileId(fileId)
                .amount(-amount)
                .type(PointType.REDUCE)
                .description(description)
                .build());

        //  URL 반환
        return s3Service.generatePresignedGetUrl(fileKey);
    }

    // 내역 조회
    public Page<PointHistoryDto> getHistory(Long userId, Pageable pageable){
        return historyRepository.findAllByUserId(userId, pageable)
                .map(PointHistoryDto::from);
    }
}
