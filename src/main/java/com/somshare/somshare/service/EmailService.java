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
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        padding: 40px 20px;
                        line-height: 1.6;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background: white;
                        border-radius: 16px;
                        overflow: hidden;
                        box-shadow: 0 20px 60px rgba(0,0,0,0.3);
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 40px 30px;
                        text-align: center;
                    }
                    .header h1 {
                        font-size: 28px;
                        font-weight: 600;
                        margin-bottom: 8px;
                    }
                    .header p {
                        font-size: 14px;
                        opacity: 0.9;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .greeting {
                        font-size: 18px;
                        color: #333;
                        margin-bottom: 20px;
                    }
                    .message {
                        color: #666;
                        font-size: 15px;
                        margin-bottom: 30px;
                        line-height: 1.8;
                    }
                    .code-section {
                        background: linear-gradient(135deg, #f5f7fa 0%%, #c3cfe2 100%%);
                        border-radius: 12px;
                        padding: 30px;
                        text-align: center;
                        margin: 30px 0;
                    }
                    .code-label {
                        font-size: 14px;
                        color: #666;
                        margin-bottom: 15px;
                        font-weight: 500;
                    }
                    .code-box {
                        background: white;
                        border: 3px dashed #667eea;
                        border-radius: 10px;
                        padding: 20px;
                        display: inline-block;
                        min-width: 280px;
                    }
                    .code {
                        font-size: 42px;
                        font-weight: 700;
                        color: #667eea;
                        letter-spacing: 8px;
                        font-family: 'Courier New', monospace;
                    }
                    .info-box {
                        background: #fff8e1;
                        border-left: 4px solid #ffc107;
                        padding: 15px 20px;
                        margin: 25px 0;
                        border-radius: 4px;
                    }
                    .info-box p {
                        color: #856404;
                        font-size: 14px;
                        margin: 5px 0;
                    }
                    .warning {
                        background: #f3e5f5;
                        border-left: 4px solid #9c27b0;
                        padding: 15px 20px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .warning p {
                        color: #6a1b9a;
                        font-size: 13px;
                    }
                    .footer {
                        background: #f8f9fa;
                        padding: 20px 30px;
                        text-align: center;
                        border-top: 1px solid #e9ecef;
                    }
                    .footer p {
                        color: #999;
                        font-size: 12px;
                        margin: 5px 0;
                    }
                    .footer .brand {
                        color: #667eea;
                        font-weight: 600;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎓 SomShare</h1>
                        <p>동덕여대 족보 공유 플랫폼</p>
                    </div>

                    <div class="content">
                        <div class="greeting">
                            안녕하세요 👋
                        </div>

                        <div class="message">
                            <strong>SomShare</strong> 회원가입을 환영합니다!<br>
                            아래의 인증 코드를 회원가입 화면에 입력하여 이메일 인증을 완료해주세요.
                        </div>

                        <div class="code-section">
                            <div class="code-label">인증 코드</div>
                            <div class="code-box">
                                <div class="code">%s</div>
                            </div>
                        </div>

                        <div class="info-box">
                            <p><strong>⏰ 유효 시간:</strong> 5분</p>
                            <p><strong>📧 이메일 인증 후:</strong> 즉시 회원가입이 가능합니다</p>
                        </div>

                        <div class="warning">
                            <p>⚠️ 본인이 요청하지 않은 인증 코드라면 이 이메일을 무시하셔도 됩니다.</p>
                        </div>
                    </div>

                    <div class="footer">
                        <p class="brand">SomShare</p>
                        <p>© 2026 SomShare. All rights reserved.</p>
                        <p>동덕여자대학교 족보 공유 플랫폼</p>
                    </div>
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
