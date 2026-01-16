package com.somshare.somshare.controller;

import com.somshare.somshare.dto.*;
import com.somshare.somshare.service.AuthService;
import com.somshare.somshare.service.VerificationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final VerificationService verificationService;

    @PostMapping("/send-verification")
    public ResponseEntity<VerificationResponse> sendVerification(@Valid @RequestBody SendVerificationRequest request) {
        verificationService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok(VerificationResponse.of("인증 코드가 이메일로 전송되었습니다."));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<VerificationResponse> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        verificationService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(VerificationResponse.of("이메일 인증이 완료되었습니다."));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse httpResponse) {
        LoginResponse response = authService.login(request);

        // HttpOnly 쿠키에 토큰 저장 (7일 유효)
        ResponseCookie cookie = ResponseCookie.from("accessToken", response.getToken())
                .httpOnly(true)
                .secure(false) // 개발 환경에서는 false, 프로덕션에서는 true로 변경
                .path("/")
                .maxAge(60 * 60 * 24 * 7) // 7일
                .sameSite("Lax")
                .build();

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile-setup")
    public ResponseEntity<ProfileSetupResponse> setupProfile(
            @RequestParam("email") String email,
            @Valid @ModelAttribute ProfileSetupRequest request
    ) throws IOException {
        ProfileSetupResponse response = authService.setupProfile(email, request);
        return ResponseEntity.ok(response);
    }
}
