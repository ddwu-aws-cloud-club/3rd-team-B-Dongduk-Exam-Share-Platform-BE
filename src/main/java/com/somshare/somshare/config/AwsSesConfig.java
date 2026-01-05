package com.somshare.somshare.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class AwsSesConfig {

    @Value("${aws.ses.access-key:}")
    private String accessKey;

    @Value("${aws.ses.secret-key:}")
    private String secretKey;

    @Value("${aws.region:ap-northeast-2}")
    private String region;

    @Bean
    @Profile("dev")
    public SesClient sesClient() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return SesClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    @Bean
    @Profile("simple")
    public SesClient sesClientMock() {
        // simple 프로필에서는 실제 SES 클라이언트 대신 null을 반환하거나
        // Mock 객체를 반환할 수 있지만, EmailService에서 프로필 체크를 하므로
        // 여기서는 더미 클라이언트를 생성
        return SesClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .build();
    }
}
