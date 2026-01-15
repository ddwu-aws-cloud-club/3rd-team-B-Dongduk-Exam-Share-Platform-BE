package com.somshare.somshare.controller;

import com.somshare.somshare.dto.ExamPostListResponse;
import com.somshare.somshare.dto.PostUploadResponse;
import com.somshare.somshare.service.ExamPostService;
import com.somshare.somshare.service.PostService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Validated
public class PostController {

    private final PostService postService;
    private final ExamPostService examPostService; // ✅ 추가

    /**
     * 전체 게시글 검색/정렬/페이징 (+ 학과 필터 연결)
     * 예) GET /api/posts?search=자료&major=컴퓨터학전공&departmentId=3&page=0&size=20&sort=latest|popular|downloads
     */
    @GetMapping
    public ExamPostListResponse getPosts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        log.info("[POSTS_LIST_REQ] search={} major={} departmentId={} page={} size={} sort={}",
                search, major, departmentId, page, size, sort);

        ExamPostListResponse res = examPostService.getPosts(search, major, departmentId, page, size, sort);

        log.info("[POSTS_LIST_RES] totalElements={} totalPages={} currentPage={}",
                res.totalElements(), res.totalPages(), res.currentPage());

        return res;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostUploadResponse uploadPost(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") @NotBlank(message = "제목을 입력해주세요.")
            @Size(min = 3, max = 100, message = "제목은 3자 이상 100자 이하로 입력해주세요.") String title,
            @RequestParam("subject") @NotBlank(message = "과목명을 입력해주세요.")
            @Size(min = 2, max = 50, message = "과목명은 2자 이상 50자 이하로 입력해주세요.") String subject,
            @RequestParam("professor") @NotBlank(message = "교수명을 입력해주세요.")
            @Size(min = 2, max = 20, message = "교수명은 2자 이상 20자 이하로 입력해주세요.") String professor,
            @RequestParam("major") @NotBlank String major,
            Principal principal
    ) throws Exception {
        String username = principal != null ? principal.getName() : null;

        log.info("[POST_UPLOAD_REQ] username={} title={} subject={} professor={} major={} fileName={}",
                username,
                safe(title),
                safe(subject),
                safe(professor),
                safe(major),
                file != null ? safe(file.getOriginalFilename()) : "null"
        );

        long start = System.currentTimeMillis();
        try {
            PostUploadResponse response = postService.uploadPost(
                    file, title, subject, professor, major, username
            );

            log.info("[POST_UPLOAD_OK] username={} postId={} elapsedMs={}",
                    username, response.id(), System.currentTimeMillis() - start);

            return response;

        } catch (Exception e) {
            log.error("[POST_UPLOAD_FAIL] username={} title={} elapsedMs={}",
                    username, safe(title), System.currentTimeMillis() - start, e);
            throw e;
        }
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replaceAll("[\\r\\n\\t]", "_");
    }
}
