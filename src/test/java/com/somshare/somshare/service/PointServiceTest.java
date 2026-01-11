package com.somshare.somshare.service;

import com.somshare.somshare.domain.PointHistory;
import com.somshare.somshare.domain.PointType;
import com.somshare.somshare.domain.User;
import com.somshare.somshare.repository.PointHistoryRepository;
import com.somshare.somshare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito 테스트 도구 장착
class PointServiceTest {

    @InjectMocks // 가짜 부품들을 조립해서 만들 "진짜" 서비스
    private PointService pointService;

    @Mock // 가짜(Mock) 부품들
    private UserRepository userRepository;
    @Mock
    private PointHistoryRepository historyRepository;
    @Mock
    private S3Client s3Client;
    @Mock
    private S3DownloadService s3DownloadService; // 우리가 새로 만든 다운로드 서비스

    private User testUser; // 테스트에 쓸 가짜 유저

    @BeforeEach
    void setUp() {
        // @Value("${aws.s3.bucket}") 값을 강제로 주입
        ReflectionTestUtils.setField(pointService, "bucketName", "test-bucket");

        // 테스트용 유저 생성 (초기 포인트 1000)
        testUser = User.builder()
                .nickname("테스트유저")
                .email("test@example.com")
                .password("password")
                .points(1000)
                .isVerified(true)
                .build();

        // User의 ID 강제 주입 (DB가 없으므로)
        ReflectionTestUtils.setField(testUser, "id", 1L);
    }

    // --- 👇 적립(Earn) 테스트 ---

    @Test
    @DisplayName("성공: 파일 업로드 확인 후 포인트가 100점 적립된다")
    void completeUploadAndEarnPoints_Success() {
        // Given (상황 연출)
        // 1. 유저 조회하면 testUser를 줘라
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(testUser));
        // 2. S3에 파일 있냐고 물으면 "응(Response객체)"이라고 대답해라
        given(s3Client.headObject(any(HeadObjectRequest.class)))
                .willReturn(HeadObjectResponse.builder().build());

        // When (실행)
        pointService.completeUploadAndEarnPoints(1L, "pdfs/test.pdf", "orig.pdf", 100L, "desc");

        // Then (검증)
        // 1000 -> 1100 포인트가 되었는지 확인
        assertThat(testUser.getPoints()).isEqualTo(1100);

        // 내역 저장(save)이 딱 1번 호출됐는지 확인
        verify(historyRepository, times(1)).save(any(PointHistory.class));
    }

    @Test
    @DisplayName("실패: S3에 파일이 없으면 에러가 발생한다")
    void completeUploadAndEarnPoints_Fail_NoFile() {
        // Given
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(testUser));

        // S3에 파일 없다고 예외 던지게 설정 (연기 지도)
        given(s3Client.headObject(any(HeadObjectRequest.class)))
                .willThrow(NoSuchKeyException.class);

        // When & Then (실행하면서 에러 검증)
        assertThatThrownBy(() ->
                pointService.completeUploadAndEarnPoints(1L, "nofile.pdf", "orig.pdf", 100L, "desc")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("S3에 파일이 존재하지 않습니다");
    }

    // --- 👇 사용(Reduce) 테스트 ---

    @Test
    @DisplayName("성공: 포인트 사용 시 잔액 차감 후 URL을 반환한다")
    void reducePoints_Success() {
        // Given
        Long fileId = 100L;
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(testUser));

        // 중복 구매 아님 (false)
        given(historyRepository.existsByUserIdAndFileIdAndType(1L, fileId, PointType.REDUCE)) // REDUCE 확인!
                .willReturn(false);

        // URL 발급 요청하면 가짜 주소 리턴
        given(s3DownloadService.generatePresignedGetUrl(anyString()))
                .willReturn("https://s3-url.com/download");

        // When
        String url = pointService.reducePoints(1L, fileId, 50, "다운로드");

        // Then
        assertThat(testUser.getPoints()).isEqualTo(950); // 1000 - 50
        assertThat(url).isEqualTo("https://s3-url.com/download");
        verify(historyRepository, times(1)).save(any(PointHistory.class)); // 저장 호출됨
    }

    @Test
    @DisplayName("성공: 이미 구매한 파일은 포인트 차감 없이 URL만 준다")
    void reducePoints_AlreadyPurchased() {
        // Given
        Long fileId = 100L;
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(testUser));

        // 이미 샀음! (True)
        given(historyRepository.existsByUserIdAndFileIdAndType(1L, fileId, PointType.REDUCE))
                .willReturn(true);

        given(s3DownloadService.generatePresignedGetUrl(anyString()))
                .willReturn("https://s3-url.com/download");

        // When
        String url = pointService.reducePoints(1L, fileId, 50, "재다운로드");

        // Then
        assertThat(testUser.getPoints()).isEqualTo(1000); // 깎이면 안됨!
        verify(historyRepository, never()).save(any(PointHistory.class)); // 내역 저장 안 함
    }
}