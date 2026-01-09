package com.somshare.somshare.service;

import com.somshare.somshare.domain.EmailVerification;
import com.somshare.somshare.exception.DuplicateEmailException;
import com.somshare.somshare.exception.InvalidVerificationCodeException;
import com.somshare.somshare.repository.EmailVerificationRepository;
import com.somshare.somshare.repository.UserRepository;
import com.somshare.somshare.util.LogMaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
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

        // 인증 코드 발송 요청 (정상 흐름 → INFO)
        log.info("AUTH email_verification_send_requested email={}",
                LogMaskingUtil.maskEmail(email));

        // 이미 가입된 이메일인지 확인
        if (userRepository.findByEmail(email).isPresent()) {
            // 예상 가능한 사용자 상태 문제 → WARN
            log.warn("AUTH email_verification_send_rejected reason=duplicate_email email={}",
                    LogMaskingUtil.maskEmail(email));
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

        // 이메일 전송 (AWS SES 또는 콘솔 로그)
        try {
            emailService.sendVerificationCode(email, code);
            // 메일 발송 성공 → INFO
            log.info("AUTH email_verification_send_success email={}",
                    LogMaskingUtil.maskEmail(email));
        } catch (Exception e) {
            // 외부 시스템(메일) 실패 → ERROR + stacktrace
            log.error("AUTH email_send_failed email={}",
                    LogMaskingUtil.maskEmail(email), e);
            throw e;
        }
    }

    @Transactional
    public void verifyCode(String email, String code) {

        try {
            // 인증 코드 조회
            EmailVerification verification = verificationRepository
                    .findByEmailAndCodeAndVerifiedFalse(email, code)
                    .orElseThrow(InvalidVerificationCodeException::new);

            // 만료 여부 확인
            if (verification.isExpired()) {
                log.warn("AUTH email_verification_failed reason=expired email={}",
                        LogMaskingUtil.maskEmail(email));
                throw new InvalidVerificationCodeException("인증 코드가 만료되었습니다. 다시 요청해주세요.");
            }

            // 인증 완료 처리
            verification.verify();

            // 인증 성공 → INFO
            log.info("AUTH email_verification_success email={}",
                    LogMaskingUtil.maskEmail(email));

        } catch (InvalidVerificationCodeException e) {
            // 코드 불일치 / 만료 → WARN
            log.warn("AUTH email_verification_failed reason=invalid_or_expired email={}",
                    LogMaskingUtil.maskEmail(email));
            throw e;
        } catch (Exception e) {
            // 예상치 못한 인증 처리 오류 → ERROR
            log.error("AUTH email_verification_error email={}",
                    LogMaskingUtil.maskEmail(email), e);
            throw e;
        }
    }

    public boolean isEmailVerified(String email) {
        return verificationRepository
                .findTopByEmailAndVerifiedTrueOrderByCreatedAtDesc(email)
                .isPresent();
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000; // 100000 ~ 999999
        return String.valueOf(code);
    }
}
