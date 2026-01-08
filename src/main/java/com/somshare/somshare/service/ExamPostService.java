package com.somshare.somshare.service;

import com.somshare.somshare.dto.ExamPostCreateRequest;
import com.somshare.somshare.dto.ExamPostResponse;
import com.somshare.somshare.dto.ExamPostUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ExamPostService {

    List<ExamPostResponse> getExamPostsByDepartment(Long departmentId);

    ExamPostResponse getExamPostDetail(Long departmentId, Long postId);

    // ✅ pdf 파라미터 추가
    ExamPostResponse createExamPost(Long departmentId,
                                    ExamPostCreateRequest request,
                                    MultipartFile pdf,
                                    String username) throws IOException;


    void deleteExamPost(Long departmentId, Long postId);

    // ✅ pdf 파라미터 추가
    ExamPostResponse updateExamPost(Long departmentId, Long postId, ExamPostUpdateRequest request, MultipartFile pdf) throws IOException;
}
