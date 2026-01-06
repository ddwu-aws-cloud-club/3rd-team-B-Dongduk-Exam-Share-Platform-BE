package com.somshare.somshare.service;

import com.somshare.somshare.dto.ExamPostResponse;

import java.util.List;

public interface ExamPostService {

    //학과별 족보 게시글 목록 조회
    List<ExamPostResponse> getExamPostsByDepartment(Long departmentId);

    //족보 게시글 상세 조회
    ExamPostResponse getExamPostDetail(Long departmentId, Long postId);
}
