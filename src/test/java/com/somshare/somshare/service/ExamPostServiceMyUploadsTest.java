package com.somshare.somshare.service;

import com.somshare.somshare.domain.ExamPost;
import com.somshare.somshare.dto.MyUploadsPageResponse;
import com.somshare.somshare.repository.ExamPostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExamPostServiceMyUploadsTest {

    @InjectMocks
    private ExamPostServiceImpl examPostService; // ✅ 실제 구현체 이름 맞으면 OK

    @Mock
    private ExamPostRepository examPostRepository;

    @Test
    @DisplayName("성공: 내 업로드 목록 조회 - findByUploaderEmail 호출 + 페이징 응답 반환")
    void getMyUploads_success() {
        // Given
        String username = "test@example.com";
        int page = 0;
        int size = 10;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        ExamPost p1 = new ExamPost();
        ReflectionTestUtils.setField(p1, "title", "경영학원론 중간고사 족보");
        ReflectionTestUtils.setField(p1, "subject", "경영학원론");
        ReflectionTestUtils.setField(p1, "professor", "이영희");

        ExamPost p2 = new ExamPost();
        ReflectionTestUtils.setField(p2, "title", "운영체제 기말 족보");
        ReflectionTestUtils.setField(p2, "subject", "운영체제");
        ReflectionTestUtils.setField(p2, "professor", "김철수");

        Page<ExamPost> repoPage = new PageImpl<>(List.of(p1, p2), pageable, 2);

        given(examPostRepository.findByUploaderEmail(eq(username), any(Pageable.class)))
                .willReturn(repoPage);

        // When
        MyUploadsPageResponse resp = examPostService.getMyUploads(username, page, size);

        // Then
        assertThat(resp).isNotNull();
        assertThat(resp.getContent()).hasSize(2);
        assertThat(resp.getTotalElements()).isEqualTo(2);
        assertThat(resp.getCurrentPage()).isEqualTo(0);

        verify(examPostRepository).findByUploaderEmail(eq(username), any(Pageable.class));
    }
}
