package com.somshare.somshare.repository;

import com.somshare.somshare.domain.ExamPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamPostRepository extends JpaRepository<ExamPost, Long> {

    // 학과별 게시글 목록 조회 (최신순)
    List<ExamPost> findByDepartment_IdOrderByCreatedAtDesc(Long departmentId);

    // 게시글 상세 조회
    Optional<ExamPost> findByIdAndDepartment_Id(Long postId, Long departmentId);

    // 사용자가 업로드한 족보 개수 조회
    Long countByUploaderId(Long uploaderId);
}
