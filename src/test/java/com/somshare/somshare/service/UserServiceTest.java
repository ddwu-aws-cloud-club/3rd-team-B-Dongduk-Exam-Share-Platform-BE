package com.somshare.somshare.service;

import com.somshare.somshare.domain.Post;
import com.somshare.somshare.domain.PointHistory;
import com.somshare.somshare.domain.PointType;
import com.somshare.somshare.domain.User;
import com.somshare.somshare.dto.DownloadHistoryResponse;
import com.somshare.somshare.dto.UserMeResponse;
import com.somshare.somshare.exception.UserNotFoundException;
import com.somshare.somshare.repository.ExamPostRepository;
import com.somshare.somshare.repository.PointHistoryRepository;
import com.somshare.somshare.repository.PostRepository;
import com.somshare.somshare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private ExamPostRepository examPostRepository;
    @Mock
    private PointHistoryRepository pointHistoryRepository;
    @Mock
    private PostRepository postRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .email("student@dongduk.ac.kr")
                .password("encodedPassword")
                .nickname("홍길동")
                .college("정보과학대학")
                .major("컴퓨터학전공")
                .profileImageUrl("https://example.com/profile.jpg")
                .points(1000)
                .isVerified(true)
                .build();

        // ID와 createdAt 강제 주입
        ReflectionTestUtils.setField(testUser, "id", 123L);
        ReflectionTestUtils.setField(testUser, "createdAt", LocalDateTime.of(2024, 3, 1, 10, 0, 0));
    }

    @Test
    @DisplayName("성공: 로그인한 사용자의 상세 정보를 조회한다")
    void getUserMe_Success() {
        // Given
        Long userId = 123L;
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(examPostRepository.countByUploaderId(userId)).willReturn(12L);
        given(pointHistoryRepository.countDownloads(userId)).willReturn(8L);
        given(pointHistoryRepository.getTotalEarnedPoints(userId)).willReturn(1200);
        given(pointHistoryRepository.getTotalSpentPoints(userId)).willReturn(400);

        // When
        UserMeResponse response = userService.getUserMe(userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(123L);
        assertThat(response.getEmail()).isEqualTo("student@dongduk.ac.kr");
        assertThat(response.getNickname()).isEqualTo("홍길동");
        assertThat(response.getCollege()).isEqualTo("정보과학대학");
        assertThat(response.getMajor()).isEqualTo("컴퓨터학전공");
        assertThat(response.getMajorCode()).isEqualTo("computer-science");
        assertThat(response.getPoints()).isEqualTo(1000);
        assertThat(response.getProfileImage()).isEqualTo("https://example.com/profile.jpg");
        assertThat(response.getIsVerified()).isTrue();
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 3, 1, 10, 0, 0));
        assertThat(response.getTotalUploads()).isEqualTo(12L);
        assertThat(response.getTotalDownloads()).isEqualTo(8L);
        assertThat(response.getTotalEarnedPoints()).isEqualTo(1200);
        assertThat(response.getTotalSpentPoints()).isEqualTo(400);

        // Repository 메서드 호출 확인
        verify(userRepository).findById(userId);
        verify(examPostRepository).countByUploaderId(userId);
        verify(pointHistoryRepository).countDownloads(userId);
        verify(pointHistoryRepository).getTotalEarnedPoints(userId);
        verify(pointHistoryRepository).getTotalSpentPoints(userId);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 사용자 조회 시 UserNotFoundException 발생")
    void getUserMe_UserNotFound() {
        // Given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserMe(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("성공: 통계가 없는 사용자는 0으로 반환된다")
    void getUserMe_NoStatistics() {
        // Given
        Long userId = 123L;
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(examPostRepository.countByUploaderId(userId)).willReturn(0L);
        given(pointHistoryRepository.countDownloads(userId)).willReturn(0L);
        given(pointHistoryRepository.getTotalEarnedPoints(userId)).willReturn(0);
        given(pointHistoryRepository.getTotalSpentPoints(userId)).willReturn(0);

        // When
        UserMeResponse response = userService.getUserMe(userId);

        // Then
        assertThat(response.getTotalUploads()).isEqualTo(0L);
        assertThat(response.getTotalDownloads()).isEqualTo(0L);
        assertThat(response.getTotalEarnedPoints()).isEqualTo(0);
        assertThat(response.getTotalSpentPoints()).isEqualTo(0);
    }

    @Test
    @DisplayName("성공: 프로필 이미지가 없는 사용자도 정상 조회된다")
    void getUserMe_NoProfileImage() {
        // Given
        User userWithoutImage = User.builder()
                .email("student@dongduk.ac.kr")
                .password("encodedPassword")
                .nickname("김철수")
                .college("인문과학대학")
                .major("국어국문학과")
                .profileImageUrl(null) // 프로필 이미지 없음
                .points(500)
                .isVerified(false)
                .build();
        ReflectionTestUtils.setField(userWithoutImage, "id", 456L);
        ReflectionTestUtils.setField(userWithoutImage, "createdAt", LocalDateTime.of(2024, 4, 1, 10, 0, 0));

        given(userRepository.findById(456L)).willReturn(Optional.of(userWithoutImage));
        given(examPostRepository.countByUploaderId(456L)).willReturn(0L);
        given(pointHistoryRepository.countDownloads(456L)).willReturn(0L);
        given(pointHistoryRepository.getTotalEarnedPoints(456L)).willReturn(0);
        given(pointHistoryRepository.getTotalSpentPoints(456L)).willReturn(0);

        // When
        UserMeResponse response = userService.getUserMe(456L);

        // Then
        assertThat(response.getProfileImage()).isNull();
        assertThat(response.getIsVerified()).isFalse();
        assertThat(response.getNickname()).isEqualTo("김철수");
    }

    // --- 다운로드 목록 조회 테스트 ---

    @Test
    @DisplayName("성공: 다운로드 목록을 페이징 처리하여 조회한다")
    void getMyDownloads_Success() {
        // Given
        Long userId = 123L;
        Pageable pageable = PageRequest.of(0, 10);

        // 테스트 Post 생성
        Post post1 = new Post("2024-2학기 운영체제 중간고사", "운영체제", "박교수",
                "컴퓨터학전공", "https://s3.aws.com/file1.pdf", "files/1.pdf", testUser);
        ReflectionTestUtils.setField(post1, "id", 25L);

        Post post2 = new Post("2024-1학기 자료구조 기말고사", "자료구조", "김교수",
                "컴퓨터학전공", "https://s3.aws.com/file2.pdf", "files/2.pdf", testUser);
        ReflectionTestUtils.setField(post2, "id", 30L);

        // 테스트 PointHistory 생성
        PointHistory history1 = PointHistory.builder()
                .user(testUser)
                .fileId(25L)
                .amount(-50)
                .type(PointType.REDUCE)
                .description("족보 다운로드")
                .build();
        ReflectionTestUtils.setField(history1, "id", 1L);
        ReflectionTestUtils.setField(history1, "createdAt", LocalDateTime.of(2024, 10, 18, 9, 20, 0));

        PointHistory history2 = PointHistory.builder()
                .user(testUser)
                .fileId(30L)
                .amount(-50)
                .type(PointType.REDUCE)
                .description("족보 다운로드")
                .build();
        ReflectionTestUtils.setField(history2, "id", 2L);
        ReflectionTestUtils.setField(history2, "createdAt", LocalDateTime.of(2024, 9, 15, 14, 30, 0));

        Page<PointHistory> historyPage = new PageImpl<>(Arrays.asList(history1, history2), pageable, 2);

        given(pointHistoryRepository.findDownloadsByUserId(userId, pageable)).willReturn(historyPage);
        given(postRepository.findById(25L)).willReturn(Optional.of(post1));
        given(postRepository.findById(30L)).willReturn(Optional.of(post2));

        // When
        DownloadHistoryResponse response = userService.getMyDownloads(userId, pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2L);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getCurrentPage()).isEqualTo(0);

        // 첫 번째 다운로드 항목 검증
        assertThat(response.getContent().get(0).getPostId()).isEqualTo(25L);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo("2024-2학기 운영체제 중간고사");
        assertThat(response.getContent().get(0).getSubject()).isEqualTo("운영체제");
        assertThat(response.getContent().get(0).getProfessor()).isEqualTo("박교수");
        assertThat(response.getContent().get(0).getPointsDeducted()).isEqualTo(50);
        assertThat(response.getContent().get(0).getPdfUrl()).isEqualTo("https://s3.aws.com/file1.pdf");

        verify(pointHistoryRepository).findDownloadsByUserId(userId, pageable);
        verify(postRepository).findById(25L);
        verify(postRepository).findById(30L);
    }

    @Test
    @DisplayName("성공: 다운로드 목록이 비어있으면 빈 리스트를 반환한다")
    void getMyDownloads_EmptyList() {
        // Given
        Long userId = 123L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<PointHistory> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

        given(pointHistoryRepository.findDownloadsByUserId(userId, pageable)).willReturn(emptyPage);

        // When
        DownloadHistoryResponse response = userService.getMyDownloads(userId, pageable);

        // Then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0L);
        assertThat(response.getTotalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("성공: Post가 삭제된 경우 기본값으로 처리한다")
    void getMyDownloads_DeletedPost() {
        // Given
        Long userId = 123L;
        Pageable pageable = PageRequest.of(0, 10);

        PointHistory history = PointHistory.builder()
                .user(testUser)
                .fileId(999L) // 삭제된 Post ID
                .amount(-50)
                .type(PointType.REDUCE)
                .description("족보 다운로드")
                .build();
        ReflectionTestUtils.setField(history, "id", 1L);
        ReflectionTestUtils.setField(history, "createdAt", LocalDateTime.of(2024, 10, 18, 9, 20, 0));

        Page<PointHistory> historyPage = new PageImpl<>(Arrays.asList(history), pageable, 1);

        given(pointHistoryRepository.findDownloadsByUserId(userId, pageable)).willReturn(historyPage);
        given(postRepository.findById(999L)).willReturn(Optional.empty()); // Post 없음

        // When
        DownloadHistoryResponse response = userService.getMyDownloads(userId, pageable);

        // Then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo("삭제된 게시글");
        assertThat(response.getContent().get(0).getSubject()).isEqualTo("-");
        assertThat(response.getContent().get(0).getProfessor()).isEqualTo("-");
        assertThat(response.getContent().get(0).getPdfUrl()).isNull();
    }
}
