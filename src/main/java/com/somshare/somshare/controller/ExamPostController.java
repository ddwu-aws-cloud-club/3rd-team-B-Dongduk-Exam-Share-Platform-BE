package com.somshare.somshare.controller;

import com.somshare.somshare.dto.ExamPostCreateRequest;
import com.somshare.somshare.dto.ExamPostResponse;
import com.somshare.somshare.service.ExamPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 족보 게시글 작성
     */
    @PostMapping("/{departmentId}/exam-posts")
    @ResponseStatus(HttpStatus.CREATED)
    public ExamPostResponse createExamPost(
            @PathVariable Long departmentId,
            @RequestBody @Valid ExamPostCreateRequest request
    ) {
        return examPostService.createExamPost(departmentId, request);
    }

    /**
     * 족보 게시글 삭제
     */
    @DeleteMapping("/{departmentId}/exam-posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExamPost(
            @PathVariable Long departmentId,
            @PathVariable Long postId
    ) {
        examPostService.deleteExamPost(departmentId, postId);
    }
}
