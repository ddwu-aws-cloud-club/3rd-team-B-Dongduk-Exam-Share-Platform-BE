package com.somshare.somshare.controller;

import com.somshare.somshare.dto.MyDownloadsPageResponse;
import com.somshare.somshare.dto.MyUploadsPageResponse;
import com.somshare.somshare.dto.UserMeResponse;
import com.somshare.somshare.security.UserPrincipal;
import com.somshare.somshare.service.ExamPostService;
import com.somshare.somshare.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User API", description = "사용자 정보 조회 API")
public class UserController {

    private final UserService userService;
    private final ExamPostService examPostService;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 상세 정보 및 통계를 조회합니다")
    public ResponseEntity<UserMeResponse> getMe(@AuthenticationPrincipal UserPrincipal user) {
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "로그인이 필요합니다. 인증 토큰을 확인해주세요."
            );
        }
        UserMeResponse response = userService.getUserMe(user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/uploads")
    public MyUploadsPageResponse getMyUploads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return examPostService.getMyUploads(principal.getUsername(), page, size);
    }

    @GetMapping("/me/downloaded-posts")
    @Operation(summary = "다운로드한 게시글 ID 목록 조회", description = "현재 로그인한 사용자가 다운로드한 게시글 ID 목록을 조회합니다")
    public ResponseEntity<List<Long>> getDownloadedPostIds(@AuthenticationPrincipal UserPrincipal user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        List<Long> postIds = userService.getDownloadedPostIds(user.getId());
        return ResponseEntity.ok(postIds);
    }

    @GetMapping("/me/downloads")
    @Operation(summary = "내 다운로드 내역 조회", description = "현재 로그인한 사용자의 다운로드 내역을 페이징 조회합니다")
    public MyDownloadsPageResponse getMyDownloads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return userService.getMyDownloads(user.getId(), page, size);
    }
}
