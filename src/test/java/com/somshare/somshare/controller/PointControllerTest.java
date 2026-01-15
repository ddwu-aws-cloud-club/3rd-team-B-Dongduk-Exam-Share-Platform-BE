package com.somshare.somshare.controller;

import com.somshare.somshare.dto.PointHistoryResponse;
import com.somshare.somshare.dto.PointReduceRequest;
import com.somshare.somshare.dto.UploadCompleteRequest;
import com.somshare.somshare.security.UserPrincipal;
import com.somshare.somshare.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class PointControllerPureTest {

    // 1. 테스트를 위한 가짜(Stub) 서비스 만들기
    // Mockito를 못 쓰므로 직접 클래스를 상속받아 메서드를 오버라이딩합니다.
    static class StubPointService extends PointService {

        // PointService의 생성자가 복잡하다면 super(null...) 등으로 처리해야 할 수도 있습니다.
        public StubPointService() {
            super(null, null, null, null); // 실제 PointService 생성자에 맞춰 null 등을 넣어주세요.
        }

        @Override
        public int getBalance(Long userId) {
            if (userId == 1L) return 5000; // 테스트용 고정값
            return 0;
        }

        @Override
        public String reducePoints(Long userId, Long fileId, String description) {
            return "https://s3.aws.com/download/test.pdf";
        }

        @Override
        public PointHistoryResponse getHistory(Long userId, String type, Pageable pageable) {
            // 빈 응답 객체 반환 (테스트 목적)
            return new PointHistoryResponse();
        }

        @Override
        public void completeUploadAndEarnPoints(Long userId, String fileName, String originalName, Long fileSize, String description) {
            // void 메서드는 아무 동작 안 함 (에러만 안 나면 성공)
        }
    }

    // 2. 테스트를 위한 가짜 UserPrincipal 만들기
    private UserPrincipal createTestUser(Long id) {
        // UserPrincipal 내부 구조를 모르므로,
        // 1) 생성자를 통해 ID를 넣거나
        // 2) Setter가 있다면 사용하거나
        // 3) 익명 클래스로 getId()만 오버라이딩해야 합니다.

        // 여기서는 가장 확실한 '익명 클래스' 방식을 사용합니다.
        return new UserPrincipal(id, "test@email.com", "password", null) {
            @Override
            public Long getId() {
                return id;
            }
        };
    }

    @Test
    @DisplayName("포인트 잔액 조회 - 순수 자바 테스트")
    void getBalance_test() {
        // given
        PointService stubService = new StubPointService(); // 가짜 서비스
        PointController controller = new PointController(stubService); // 컨트롤러 직접 생성
        UserPrincipal user = createTestUser(1L);

        // when
        ResponseEntity<Integer> response = controller.getBalance(user);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5000, response.getBody());
    }

    @Test
    @DisplayName("포인트 사용(차감) - 순수 자바 테스트")
    void reducePoints_test() {
        // given
        PointController controller = new PointController(new StubPointService());
        UserPrincipal user = createTestUser(1L);

        PointReduceRequest request = new PointReduceRequest();
        // request.setFileId(100L); // DTO 설정

        // when
        ResponseEntity<String> response = controller.reducePoints(user, request);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("https://s3.aws.com/download/test.pdf", response.getBody());
    }

    @Test
    @DisplayName("업로드 완료 - 순수 자바 테스트")
    void completeUpload_test() {
        // given
        PointController controller = new PointController(new StubPointService());
        UserPrincipal user = createTestUser(1L);
        UploadCompleteRequest request = new UploadCompleteRequest();

        // when
        ResponseEntity<Void> response = controller.completeUpload(user, request);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("로그인 안 된 유저(Null) 접근 시 예외 발생 확인")
    void unauthorized_test() {
        // given
        PointController controller = new PointController(new StubPointService());

        // when & then
        // assertThrows를 사용해 예외가 발생하는지 검증
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            controller.getBalance(null); // 유저가 null일 때
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
}