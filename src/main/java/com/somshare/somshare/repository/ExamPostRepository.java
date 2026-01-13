package com.somshare.somshare.repository;

import com.somshare.somshare.domain.ExamPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamPostRepository extends JpaRepository<ExamPost, Long> {

    List<ExamPost> findByDepartment_IdOrderByCreatedAtDesc(Long departmentId);

    Optional<ExamPost> findByIdAndDepartment_Id(Long postId, Long departmentId);

    @Query("""
        select p
        from ExamPost p
        join p.department d
        where (:departmentName is null or d.name = :departmentName)
          and (
                :search is null or :search = '' or
                lower(p.title) like lower(concat('%', :search, '%')) or
                lower(p.content) like lower(concat('%', :search, '%')) or
                lower(p.subject) like lower(concat('%', :search, '%')) or
                lower(p.professor) like lower(concat('%', :search, '%'))
          )
        """)
    Page<ExamPost> searchPosts(
            @Param("departmentName") String departmentName,
            @Param("search") String search,
            Pageable pageable
    );
}
