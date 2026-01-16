package com.somshare.somshare.controller;

import com.somshare.somshare.dto.PostListResponse;
import com.somshare.somshare.dto.PostUpdateRequest;
import com.somshare.somshare.dto.PostUploadResponse;
import com.somshare.somshare.dto.RatingResponse;
import com.somshare.somshare.security.UserPrincipal;
import com.somshare.somshare.service.PostService;
import com.somshare.somshare.service.RatingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final RatingService ratingService;

    @GetMapping
    public PostListResponse getPosts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String college,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("[POST_LIST_REQ] search={} major={} college={} page={} size={}", search, major, college, page, size);
        return postService.getPosts(search, major, college, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostUploadResponse uploadPost(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") @NotBlank(message = "제목을 입력해주세요.") @Size(min = 3, max = 100, message = "제목은 3자 이상 100자 이하로 입력해주세요.") String title,
            @RequestParam("subject") @NotBlank(message = "과목명을 입력해주세요.") @Size(min = 2, max = 50, message = "과목명은 2자 이상 50자 이하로 입력해주세요.") String subject,
            @RequestParam("professor") @NotBlank(message = "교수명을 입력해주세요.") @Size(min = 2, max = 20, message = "교수명은 2자 이상 20자 이하로 입력해주세요.") String professor,
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

    // 게시글 수정
    @PatchMapping("/{postId}")
    public PostUploadResponse updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        if (user == null) {
            throw new SecurityException("로그인이 필요합니다.");
        }

        log.info("[POST_UPDATE_REQ] postId={} user={} title={}", postId, user.getEmail(), safe(request.title()));
        return postService.updatePost(postId, request, user.getEmail());
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        if (user == null) {
            throw new SecurityException("로그인이 필요합니다.");
        }

        log.info("[POST_DELETE_REQ] postId={} user={}", postId, user.getEmail());
        postService.deletePost(postId, user.getEmail());
    }

    // 게시글 평가 (좋아요/싫어요)
    @PostMapping("/{postId}/rate")
    public RatingResponse ratePost(
            @PathVariable Long postId,
            @RequestParam @NotBlank String type, // "like" or "dislike"
            @AuthenticationPrincipal UserPrincipal user
    ) {
        if (user == null) {
            throw new SecurityException("로그인이 필요합니다.");
        }

        log.info("[RATING_REQ] postId={} type={} user={}", postId, type, user.getEmail());
        return ratingService.ratePost(postId, type, user.getEmail());
    }

    // 게시글 평가 수 조회
    @GetMapping("/{postId}/rating")
    public RatingResponse getRating(@PathVariable Long postId, @AuthenticationPrincipal UserPrincipal user) {
        String userEmail = user != null ? user.getEmail() : null;
        return ratingService.getRatingCountsWithUserRating(postId, userEmail);
    }
}
