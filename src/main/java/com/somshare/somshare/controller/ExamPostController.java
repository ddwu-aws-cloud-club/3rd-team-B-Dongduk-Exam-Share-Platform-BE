package com.somshare.somshare.controller;

import com.somshare.somshare.dto.ExamPostCreateForm;
import com.somshare.somshare.dto.ExamPostCreateRequest;
import com.somshare.somshare.dto.ExamPostResponse;
import com.somshare.somshare.dto.ExamPostUpdateRequest;
import com.somshare.somshare.service.ExamPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
     * ✅ 족보 게시글 작성 (Swagger 친화)
     * - multipart/form-data
     * - title/content/uploaderId 는 폼 필드로 입력
     * - pdf는 파일로 선택
     */
    @PostMapping(value = "/{departmentId}/exam-posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ExamPostResponse createExamPost(
            @PathVariable Long departmentId,
            @ModelAttribute @Valid ExamPostCreateForm form,
            java.security.Principal principal
    ) throws IOException {
        ExamPostCreateRequest request = new ExamPostCreateRequest(form.getTitle(), form.getContent());

        // ✅ 로그인한 사용자 식별자(username/email)
        String username = principal.getName();

        return examPostService.createExamPost(departmentId, request, form.getPdf(), username);
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

    /**
     * ✅ 족보 게시글 수정 (Swagger 친화)
     * - multipart/form-data
     * - title/content는 폼 필드로 입력
     * - pdf를 보내면 교체(서비스 로직에서 처리)
     */
    @PatchMapping(value = "/{departmentId}/exam-posts/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ExamPostResponse updateExamPost(
            @PathVariable Long departmentId,
            @PathVariable Long postId,
            @ModelAttribute @Valid ExamPostUpdateRequest request,
            @RequestPart(value = "pdf", required = false) MultipartFile pdf
    ) throws IOException {
        return examPostService.updateExamPost(departmentId, postId, request, pdf);
    }
}
