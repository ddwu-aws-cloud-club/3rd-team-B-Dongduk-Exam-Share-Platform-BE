package com.somshare.somshare.service;

import com.somshare.somshare.dto.ExamPostCreateRequest;
import com.somshare.somshare.dto.ExamPostResponse;

import java.util.List;

public interface ExamPostService {
    List<ExamPostResponse> getExamPostsByDepartment(Long departmentId);
    ExamPostResponse getExamPostDetail(Long departmentId, Long postId);

    ExamPostResponse createExamPost(Long departmentId, ExamPostCreateRequest request);
}
