package com.somshare.somshare.controller;

import com.somshare.somshare.dto.DownloadHistoryResponse;
import com.somshare.somshare.dto.UserMeResponse;
import com.somshare.somshare.security.UserPrincipal;
import com.somshare.somshare.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "사용자 정보 조회 API")
public class UserController {

    private final UserService userService;

    /**
     * 현재 로그인한 사용자의 상세 정보 조회
     * JWT 토큰에서 사용자 ID를 추출하여 조회
     *
     * @param user JWT 토큰으로 인증된 사용자 정보
     * @return UserMeResponse 사용자 상세 정보 및 통계
     */
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

    /**
     * 현재 로그인한 사용자의 다운로드 목록 조회
     * JWT 토큰에서 사용자 ID를 추출하여 다운로드한 족보 목록을 조회
     *
     * @param user JWT 토큰으로 인증된 사용자 정보
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return DownloadHistoryResponse 다운로드 목록 및 페이징 정보
     */
    @GetMapping("/me/downloads")
    @Operation(summary = "내 다운로드 목록 조회", description = "현재 로그인한 사용자가 다운로드한 족보 목록을 조회합니다 (페이징)")
    public ResponseEntity<DownloadHistoryResponse> getMyDownloads(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "로그인이 필요합니다. 인증 토큰을 확인해주세요."
            );
        }

        Pageable pageable = PageRequest.of(page, size);
        DownloadHistoryResponse response = userService.getMyDownloads(user.getId(), pageable);
        return ResponseEntity.ok(response);
    }
}
