package com.somshare.somshare.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    /**
     * 이메일로 인증 코드 전송
     * 현재는 개발/테스트 환경이므로 콘솔에 로그만 출력
     * 나중에 AWS SES로 실제 이메일 전송 기능으로 교체 가능
     */
    public void sendVerificationCode(String email, String code) {
        log.info("========================================");
        log.info("📧 이메일 인증 코드 전송");
        log.info("받는 사람: {}", email);
        log.info("인증 코드: {}", code);
        log.info("유효 시간: 5분");
        log.info("========================================");

        // TODO: AWS SES 연동 시 아래 코드로 교체
        // sesClient.sendEmail(...)
    }
}
