package com.somshare.somshare.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final SesClient sesClient;

    @Value("${aws.ses.from-email:noreply@somshare.com}")
    private String fromEmail;

    @Value("${spring.profiles.active:simple}")
    private String activeProfile;

    /**
     * 이메일로 인증 코드 전송
     * - dev 프로필: AWS SES로 실제 이메일 전송
     * - simple 프로필: 콘솔 로그만 출력 (테스트용)
     */
    public void sendVerificationCode(String toEmail, String code) {
        // simple 프로필일 경우 콘솔 로그만 출력
        if ("simple".equals(activeProfile)) {
            logVerificationCodeToConsole(toEmail, code);
            return;
        }

        // dev 프로필일 경우 실제 AWS SES로 전송
        try {
            sendEmailViaSES(toEmail, code);
            log.info("이메일 전송 성공: {}", toEmail);
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", toEmail, e);
            // 실패 시 콘솔에 로그 출력 (개발 환경 백업)
            logVerificationCodeToConsole(toEmail, code);
            throw new RuntimeException("이메일 전송에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    private void sendEmailViaSES(String toEmail, String code) {
        String subject = "[SomShare] 이메일 인증 코드";
        String htmlBody = buildEmailHtml(code);
        String textBody = buildEmailText(code);

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder()
                        .toAddresses(toEmail)
                        .build())
                .message(Message.builder()
                        .subject(Content.builder()
                                .charset("UTF-8")
                                .data(subject)
                                .build())
                        .body(Body.builder()
                                .html(Content.builder()
                                        .charset("UTF-8")
                                        .data(htmlBody)
                                        .build())
                                .text(Content.builder()
                                        .charset("UTF-8")
                                        .data(textBody)
                                        .build())
                                .build())
                        .build())
                .build();

        sesClient.sendEmail(emailRequest);
    }

    private String buildEmailHtml(String code) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f5f5f5; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 8px; padding: 40px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                    <h2 style="color: #333; margin-bottom: 20px;">SomShare 이메일 인증</h2>
                    <p style="margin-bottom: 20px;">안녕하세요,</p>
                    <p style="margin-bottom: 30px;">SomShare 회원가입을 위한 이메일 인증 코드입니다.</p>

                    <div style="background-color: #e3f2fd; border-radius: 8px; padding: 30px; text-align: center; margin: 30px 0;">
                        <h1 style="color: #1976d2; margin: 0; font-size: 36px; letter-spacing: 8px; font-family: 'Courier New', monospace;">%s</h1>
                    </div>

                    <p style="margin: 20px 0; color: #666;"><strong>유효 시간:</strong> 5분</p>

                    <div style="background-color: #fff3e0; border-left: 4px solid #ff9800; padding: 15px; margin: 20px 0; border-radius: 4px;">
                        <p style="margin: 0; color: #e65100; font-size: 14px;">⚠️ 본인이 요청하지 않은 인증 코드라면 이 이메일을 무시하셔도 됩니다.</p>
                    </div>

                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #ddd;">
                    <p style="color: #999; font-size: 12px; text-align: center; margin: 0;">© 2026 SomShare. 동덕여대 족보 공유 플랫폼</p>
                </div>
            </body>
            </html>
            """, code);
    }

    private String buildEmailText(String code) {
        return String.format("""
            [SomShare 이메일 인증]

            안녕하세요,
            SomShare 회원가입을 위한 이메일 인증 코드입니다.

            인증 코드: %s

            유효 시간: 5분

            본인이 요청하지 않은 경우 이 이메일을 무시하셔도 됩니다.

            © 2026 SomShare. 동덕여대 족보 공유 플랫폼
            """, code);
    }

    private void logVerificationCodeToConsole(String email, String code) {
        log.info("========================================");
        log.info("📧 이메일 인증 코드 전송 (콘솔 모드)");
        log.info("받는 사람: {}", email);
        log.info("인증 코드: {}", code);
        log.info("유효 시간: 5분");
        log.info("========================================");
    }
}
