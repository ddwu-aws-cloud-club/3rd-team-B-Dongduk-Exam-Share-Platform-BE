package com.somshare.somshare.controller;

import com.somshare.somshare.dto.ExamPostCreateRequest;
import com.somshare.somshare.dto.ExamPostResponse;
import com.somshare.somshare.dto.ExamPostUpdateRequest;
import com.somshare.somshare.service.ExamPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/departments")
public class ExamPostController {

    private final ExamPostService examPostService;

    @GetMapping("/{departmentId}/exam-posts")
    public List<ExamPostResponse> getExamPosts(@PathVariable Long departmentId) {
        return examPostService.getExamPostsByDepartment(departmentId);
    }

    @GetMapping("/{departmentId}/exam-posts/{postId}")
    public ExamPostResponse getExamPostDetail(
            @PathVariable Long departmentId,
            @PathVariable Long postId
    ) {
        return examPostService.getExamPostDetail(departmentId, postId);
    }

    // ✅ JSON 기반 생성 (업로드 결과(fileKey/fileUrl)를 받아 DB에 저장만)
    @PostMapping("/{departmentId}/exam-posts")
    @ResponseStatus(HttpStatus.CREATED)
    public ExamPostResponse createExamPost(
            @PathVariable Long departmentId,
            @RequestBody @Valid ExamPostCreateRequest request,
            Principal principal
    ) {
        String username = principal.getName();
        return examPostService.createExamPost(departmentId, request, username);
    }

    @PatchMapping("/{departmentId}/exam-posts/{postId}")
    public ExamPostResponse updateExamPost(
            @PathVariable Long departmentId,
            @PathVariable Long postId,
            @RequestBody @Valid ExamPostUpdateRequest request,
            Principal principal
    ) {
        String username = principal.getName();
        return examPostService.updateExamPost(departmentId, postId, request, username);
    }

    @DeleteMapping("/{departmentId}/exam-posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExamPost(
            @PathVariable Long departmentId,
            @PathVariable Long postId,
            Principal principal
    ) {
        String username = principal.getName();
        examPostService.deleteExamPost(departmentId, postId, username);
    }
}
