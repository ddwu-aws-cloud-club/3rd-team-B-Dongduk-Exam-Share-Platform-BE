package com.somshare.somshare.service;

import com.somshare.somshare.domain.EmailVerification;
import com.somshare.somshare.exception.DuplicateEmailException;
import com.somshare.somshare.exception.InvalidVerificationCodeException;
import com.somshare.somshare.repository.EmailVerificationRepository;
import com.somshare.somshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VerificationService {

    private final EmailVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 5;

    @Transactional
    public void sendVerificationCode(String email) {
        // 이미 가입된 이메일인지 확인
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateEmailException();
        }

        // 6자리 랜덤 인증 코드 생성
        String code = generateVerificationCode();

        // 만료 시간 설정 (5분)
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

        // 기존 미인증 코드가 있으면 삭제 (새로운 코드로 대체)
        verificationRepository.findByEmailAndVerifiedFalse(email)
                .ifPresent(verificationRepository::delete);

        // 새 인증 코드 저장
        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code(code)
                .expiresAt(expiresAt)
                .build();

        verificationRepository.save(verification);

        // 이메일 전송 (현재는 콘솔 로그)
        emailService.sendVerificationCode(email, code);
    }

    @Transactional
    public void verifyCode(String email, String code) {
        // 인증 코드 조회
        EmailVerification verification = verificationRepository
                .findByEmailAndCodeAndVerifiedFalse(email, code)
                .orElseThrow(InvalidVerificationCodeException::new);

        // 만료 여부 확인
        if (verification.isExpired()) {
            throw new InvalidVerificationCodeException("인증 코드가 만료되었습니다. 다시 요청해주세요.");
        }

        // 인증 완료 처리
        verification.verify();
    }

    public boolean isEmailVerified(String email) {
        return verificationRepository.findByEmailAndVerifiedFalse(email)
                .map(EmailVerification::isVerified)
                .orElse(false);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000; // 100000 ~ 999999
        return String.valueOf(code);
    }
}
