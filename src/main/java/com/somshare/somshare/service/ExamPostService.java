package com.somshare.somshare.service;

import com.somshare.somshare.dto.*;

import java.util.List;

public interface ExamPostService {

    List<ExamPostResponse> getExamPostsByDepartment(Long departmentId);

    ExamPostListResponse getPosts(String search, String major, int page, int size, String sort);

    ExamPostResponse getExamPostDetail(Long departmentId, Long postId);

    ExamPostResponse createExamPost(Long departmentId, ExamPostCreateRequest request, String username);

    ExamPostResponse updateExamPost(Long departmentId, Long postId, ExamPostUpdateRequest request, String username);

    void deleteExamPost(Long departmentId, Long postId, String username);
}
