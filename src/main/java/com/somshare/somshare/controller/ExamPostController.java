package com.somshare.somshare.controller;

import com.somshare.somshare.dto.ExamPostResponse;
import com.somshare.somshare.service.ExamPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.somshare.somshare.dto.ExamPostCreateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/departments")
public class ExamPostController {

    private final ExamPostService examPostService;

    /**
     * 학과별 족보 게시글 목록 조회
     */
    @GetMapping("/{departmentId}/exam-posts")
    public List<ExamPostResponse> getExamPosts(@PathVariable Long departmentId) {
        return examPostService.getExamPostsByDepartment(departmentId);
    }

    /**
     * 족보 게시글 상세 조회
     */
    @GetMapping("/{departmentId}/exam-posts/{postId}")
    public ExamPostResponse getExamPostDetail(
            @PathVariable Long departmentId,
            @PathVariable Long postId
    ) {
        return examPostService.getExamPostDetail(departmentId, postId);
    }
    @PostMapping("/{departmentId}/exam-posts")
    @ResponseStatus(HttpStatus.CREATED)
    public ExamPostResponse createExamPost(
            @PathVariable Long departmentId,
            @RequestBody @Valid ExamPostCreateRequest request
    ) {
        return examPostService.createExamPost(departmentId, request);
    }

}
