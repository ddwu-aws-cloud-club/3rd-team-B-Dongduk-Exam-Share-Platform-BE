package com.somshare.somshare.controller;

import com.somshare.somshare.dto.PointHistoryDto;
import com.somshare.somshare.dto.PointReduceRequest;
import com.somshare.somshare.dto.UploadCompleteRequest;
import com.somshare.somshare.security.UserPrincipal;
import com.somshare.somshare.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
@Tag(name = "Point API", description = "포인트 관련 조회, 적립, 사용 API")
public class PointController {
    private final PointService pointService;

    //테스트를 용이하게 하려고 바꾼 부분
    //이외에도 user.getId를 써야하는 곳에 전부 userId로 변경해둠
    private Long getUserIdForSwagger(UserPrincipal user) {
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "로그인이 필요합니다. 인증 토큰을 확인해주세요."
            );
        }
        return user.getId();
    }

    // 조회
    @GetMapping("/balance")
    @Operation(summary = "내 포인트 잔액 조회")
    public ResponseEntity<Integer> getBalance(@AuthenticationPrincipal UserPrincipal user){
        Long userId = getUserIdForSwagger(user);

        int balance = pointService.getBalance(userId);
        return ResponseEntity.ok(balance);
    }

    // 사용
    @PostMapping("/reduce")
    @Operation(summary = "포인트 사용", description = "자료 다운로드 시 포인트 차감")
    public ResponseEntity<String> reducePoints(
                                                @AuthenticationPrincipal UserPrincipal user,
                                                @RequestBody PointReduceRequest request) {

        // 서비스 호출
        String downloadUrl = pointService.reducePoints(
                user.getId(),
                request.getFileId(),
                request.getPoints(),
                request.getDescription()
        );

        // 프론트엔드에 URL 반환
        return ResponseEntity.ok(downloadUrl);
    }

    // 내역 조회
    @GetMapping("/history")
    @Operation(summary = "포인트 변동 내역 조회", description = "페이징 처리됨 (page, size, sort 파라미터 사용)")
    public ResponseEntity<Page<PointHistoryDto>> getHistory(
            @AuthenticationPrincipal UserPrincipal user,
            @Parameter(hidden = true) Pageable pageable) {

        Long userId = getUserIdForSwagger(user);

       return ResponseEntity.ok(pointService.getHistory(userId, pageable));
    }

    // 적립 -> 업로드 확인 및 적립
    @PostMapping("/upload-complete")
    @Operation(summary = "업로드 완료 및 포인트 적립", description = "S3 업로드 후 이 API를 호출해야 파일 정보가 저장되고 포인트가 적립됩니다.")
    public ResponseEntity<Void> completeUpload(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody UploadCompleteRequest request) {

        // 서비스로 넘기기
        pointService.completeUploadAndEarnPoints(
                user.getId(),
                request.getFileName(),
                request.getOriginalName(),
                request.getFileSize(),
                request.getDescription()
        );

        return ResponseEntity.ok().build();
    }
}
