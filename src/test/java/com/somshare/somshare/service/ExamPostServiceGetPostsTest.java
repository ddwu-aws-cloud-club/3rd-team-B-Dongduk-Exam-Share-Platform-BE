package com.somshare.somshare.service;

import com.somshare.somshare.domain.ExamPost;
import com.somshare.somshare.dto.ExamPostListResponse;
import com.somshare.somshare.repository.DepartmentRepository;
import com.somshare.somshare.repository.ExamPostRepository;
import com.somshare.somshare.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExamPostServiceGetPostsTest {

    @InjectMocks
    private ExamPostServiceImpl examPostService;

    @Mock private ExamPostRepository examPostRepository;
    @Mock private DepartmentRepository departmentRepository; // 생성자 주입 때문에 필요
    @Mock private UserRepository userRepository;             // 생성자 주입 때문에 필요
    @Mock private S3FileStorageService s3FileStorageService; // 생성자 주입 때문에 필요

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Test
    @DisplayName("성공: sort=latest면 createdAt DESC로 searchPosts 호출")
    void getPosts_sort_latest() {
        // Given
        String search = "운영체제";
        String major = "컴퓨터학과";
        int page = 0;
        int size = 20;
        String sort = "latest";

        ExamPost p1 = new ExamPost();
        ReflectionTestUtils.setField(p1, "title", "운영체제 기말 족보");

        Page<ExamPost> repoPage = new PageImpl<>(List.of(p1), PageRequest.of(page, size), 1);

        given(examPostRepository.searchPosts(eq(major), eq(search), any(Pageable.class)))
                .willReturn(repoPage);

        // When
        ExamPostListResponse resp = examPostService.getPosts(search, major, page, size, sort);

        // Then
        assertThat(resp).isNotNull();
        assertThat(resp.content()).hasSize(1);
        assertThat(resp.totalElements()).isEqualTo(1);
        assertThat(resp.currentPage()).isEqualTo(0);

        verify(examPostRepository).searchPosts(eq(major), eq(search), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        Sort.Order order = pageable.getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("성공: sort=downloads면 downloadCount DESC (그 다음 createdAt DESC)로 searchPosts 호출")
    void getPosts_sort_downloads() {
        // Given
        String search = "";
        String major = "컴퓨터학과";
        int page = 0;
        int size = 20;
        String sort = "downloads";

        given(examPostRepository.searchPosts(eq(major), eq(search), any(Pageable.class)))
                .willReturn(Page.empty());

        // When
        ExamPostListResponse resp = examPostService.getPosts(search, major, page, size, sort);

        // Then
        assertThat(resp).isNotNull();
        verify(examPostRepository).searchPosts(eq(major), eq(search), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();

        Sort.Order o1 = pageable.getSort().getOrderFor("downloadCount");
        assertThat(o1).isNotNull();
        assertThat(o1.getDirection()).isEqualTo(Sort.Direction.DESC);

        Sort.Order o2 = pageable.getSort().getOrderFor("createdAt");
        assertThat(o2).isNotNull();
        assertThat(o2.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("성공: sort=popular면 points DESC (그 다음 createdAt DESC)로 searchPosts 호출")
    void getPosts_sort_popular() {
        // Given
        String search = null;
        String major = "컴퓨터학과";
        int page = 0;
        int size = 20;
        String sort = "popular";

        given(examPostRepository.searchPosts(eq(major), isNull(), any(Pageable.class)))
                .willReturn(Page.empty());

        // When
        ExamPostListResponse resp = examPostService.getPosts(search, major, page, size, sort);

        // Then
        assertThat(resp).isNotNull();
        verify(examPostRepository).searchPosts(eq(major), isNull(), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();

        Sort.Order o1 = pageable.getSort().getOrderFor("points");
        assertThat(o1).isNotNull();
        assertThat(o1.getDirection()).isEqualTo(Sort.Direction.DESC);

        Sort.Order o2 = pageable.getSort().getOrderFor("createdAt");
        assertThat(o2).isNotNull();
        assertThat(o2.getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}
