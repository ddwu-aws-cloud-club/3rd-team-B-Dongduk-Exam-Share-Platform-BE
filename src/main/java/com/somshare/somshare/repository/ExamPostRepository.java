package com.somshare.somshare.repository;

import com.somshare.somshare.domain.ExamPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamPostRepository extends JpaRepository<ExamPost, Long> {

    List<ExamPost> findByDepartment_IdOrderByCreatedAtDesc(Long departmentId);
}
