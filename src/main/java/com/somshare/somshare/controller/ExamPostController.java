package com.somshare.somshare.controller;

import com.somshare.somshare.dto.ExamPostCreateRequest;
import com.somshare.somshare.dto.ExamPostResponse;
import com.somshare.somshare.dto.ExamPostUpdateRequest;
import com.somshare.somshare.service.ExamPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/departments")
public class ExamPostController {

    private final ExamPostService examPostService;

    @GetMapping("/{departmentId}/exam-posts")
    public List<ExamPostResponse> getExamPosts(@PathVariable Long departmentId) {
        log.info("[EXAM_POST_LIST_REQ] departmentId={}", departmentId);

        List<ExamPostResponse> res = examPostService.getExamPostsByDepartment(departmentId);

        log.info("[EXAM_POST_LIST_RES] departmentId={} size={}", departmentId, res == null ? 0 : res.size());
        return res;
    }

    @GetMapping("/{departmentId}/exam-posts/{postId}")
    public ExamPostResponse getExamPostDetail(
            @PathVariable Long departmentId,
            @PathVariable Long postId
    ) {
        log.info("[EXAM_POST_DETAIL_REQ] departmentId={} postId={}", departmentId, postId);

        ExamPostResponse res = examPostService.getExamPostDetail(departmentId, postId);

        log.info("[EXAM_POST_DETAIL_RES] departmentId={} postId={}", departmentId, postId);
        return res;
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

        String fileKey = safe(request.fileKey());
        String fileUrl = safe(request.fileUrl());

        // 업로드 결과(키/URL)가 들어왔는지 추적
        log.info("[EXAM_POST_CREATE_REQ] departmentId={} username={} title={} fileKey={} fileUrl={}",
                departmentId,
                username,
                safe(request.title()),
                fileKey.isEmpty() ? "null/empty" : fileKey,
                fileUrl.isEmpty() ? "null/empty" : "(exists)"
        );

        long start = System.currentTimeMillis();
        try {
            ExamPostResponse res = examPostService.createExamPost(departmentId, request, username);

            // ExamPostResponse에 postId 같은게 있으면 여기에 같이 찍는 게 베스트
            log.info("[EXAM_POST_CREATE_OK] departmentId={} username={} elapsedMs={}",
                    departmentId,
                    username,
                    System.currentTimeMillis() - start
            );

            return res;

        } catch (Exception e) {
            log.error("[EXAM_POST_CREATE_FAIL] departmentId={} username={} elapsedMs={}",
                    departmentId,
                    username,
                    System.currentTimeMillis() - start,
                    e
            );
            throw e;
        }
    }

    @PatchMapping("/{departmentId}/exam-posts/{postId}")
    public ExamPostResponse updateExamPost(
            @PathVariable Long departmentId,
            @PathVariable Long postId,
            @RequestBody @Valid ExamPostUpdateRequest request,
            Principal principal
    ) {
        String username = principal.getName();

        log.info("[EXAM_POST_UPDATE_REQ] departmentId={} postId={} username={}",
                departmentId, postId, username);

        long start = System.currentTimeMillis();
        try {
            ExamPostResponse res = examPostService.updateExamPost(departmentId, postId, request, username);

            log.info("[EXAM_POST_UPDATE_OK] departmentId={} postId={} username={} elapsedMs={}",
                    departmentId, postId, username, System.currentTimeMillis() - start);

            return res;

        } catch (Exception e) {
            log.error("[EXAM_POST_UPDATE_FAIL] departmentId={} postId={} username={} elapsedMs={}",
                    departmentId, postId, username, System.currentTimeMillis() - start, e);
            throw e;
        }
    }

    @DeleteMapping("/{departmentId}/exam-posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExamPost(
            @PathVariable Long departmentId,
            @PathVariable Long postId,
            Principal principal
    ) {
        String username = principal.getName();

        log.info("[EXAM_POST_DELETE_REQ] departmentId={} postId={} username={}",
                departmentId, postId, username);

        long start = System.currentTimeMillis();
        try {
            examPostService.deleteExamPost(departmentId, postId, username);

            log.info("[EXAM_POST_DELETE_OK] departmentId={} postId={} username={} elapsedMs={}",
                    departmentId, postId, username, System.currentTimeMillis() - start);

        } catch (Exception e) {
            log.error("[EXAM_POST_DELETE_FAIL] departmentId={} postId={} username={} elapsedMs={}",
                    departmentId, postId, username, System.currentTimeMillis() - start, e);
            throw e;
        }
    }


    private String safe(String s) {
        if (s == null) return "";
        return s.replaceAll("[\\r\\n\\t]", "_");
    }
}
