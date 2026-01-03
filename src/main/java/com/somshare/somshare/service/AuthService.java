package com.somshare.somshare.service;

import com.somshare.somshare.domain.User;
import com.somshare.somshare.dto.LoginRequest;
import com.somshare.somshare.dto.LoginResponse;
import com.somshare.somshare.dto.SignupRequest;
import com.somshare.somshare.dto.SignupResponse;
import com.somshare.somshare.exception.DuplicateEmailException;
import com.somshare.somshare.exception.EmailNotVerifiedBeforeSignupException;
import com.somshare.somshare.exception.InvalidCredentialsException;
import com.somshare.somshare.exception.UserNotFoundException;
import com.somshare.somshare.repository.EmailVerificationRepository;
import com.somshare.somshare.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository verificationRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${jwt.secret:somshare-secret-key-for-jwt-token-generation-minimum-256-bits}")
    private String jwtSecretKey;

    @Value("${jwt.expiration:86400000}") // 24시간
    private Long jwtExpiration;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 이메일 인증 확인
        boolean isEmailVerified = verificationRepository.findByEmailAndVerifiedFalse(request.getEmail())
                .map(verification -> verification.isVerified())
                .orElse(false);

        if (!isEmailVerified) {
            throw new EmailNotVerifiedBeforeSignupException();
        }

        // 이메일 중복 체크
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException();
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // User 엔티티 생성 (이메일 인증 완료 상태로 생성)
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .points(0)
                .isVerified(true)
                .build();

        // 저장
        userRepository.save(user);

        return SignupResponse.of(user.getEmail(), "회원가입이 완료되었습니다.");
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // JWT 토큰 생성
        String token = generateToken(user);

        return LoginResponse.of(token, user.getEmail(), user.getPoints(), user.getIsVerified());
    }

    private String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
