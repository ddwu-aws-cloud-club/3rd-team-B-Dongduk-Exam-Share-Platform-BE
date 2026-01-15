package com.somshare.somshare.repository;

import com.somshare.somshare.domain.ExamPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamPostRepository extends JpaRepository<ExamPost, Long> {

    // 학과별 게시글 목록 조회 (최신순)
    List<ExamPost> findByDepartment_IdOrderByCreatedAtDesc(Long departmentId);

    // 게시글 상세 조회
    Optional<ExamPost> findByIdAndDepartment_Id(Long postId, Long departmentId);

    // 내 업로드 목록 (정렬은 pageable로)
    Page<ExamPost> findByUploaderEmail(String email, Pageable pageable);

    /**
     * (기존) major(학과명) + search 기반 검색/페이징
     */
    @Query("""
        select p
        from ExamPost p
        join p.department d
        where (:departmentName is null or :departmentName = '' or lower(:departmentName) = 'all' or d.name = :departmentName)
          and (
                :search is null or :search = '' or
                lower(p.title) like lower(concat('%', :search, '%')) or
                lower(coalesce(p.content, '')) like lower(concat('%', :search, '%')) or
                lower(coalesce(p.subject, '')) like lower(concat('%', :search, '%')) or
                lower(coalesce(p.professor, '')) like lower(concat('%', :search, '%'))
          )
        """)
    Page<ExamPost> searchPosts(
            @Param("departmentName") String departmentName,
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * (추가) departmentId까지 포함한 검색/페이징
     */
    @Query("""
        select p
        from ExamPost p
        join p.department d
        where (:departmentId is null or d.id = :departmentId)
          and (:departmentName is null or :departmentName = '' or lower(:departmentName) = 'all' or d.name = :departmentName)
          and (
                :search is null or :search = '' or
                lower(p.title) like lower(concat('%', :search, '%')) or
                lower(coalesce(p.content, '')) like lower(concat('%', :search, '%')) or
                lower(coalesce(p.subject, '')) like lower(concat('%', :search, '%')) or
                lower(coalesce(p.professor, '')) like lower(concat('%', :search, '%'))
          )
        """)
    Page<ExamPost> searchPostsByDepartmentId(
            @Param("departmentId") Long departmentId,
            @Param("departmentName") String departmentName,
            @Param("search") String search,
            Pageable pageable
    );

    // 사용자가 업로드한 족보 개수 조회
    Long countByUploaderId(Long uploaderId);
}
