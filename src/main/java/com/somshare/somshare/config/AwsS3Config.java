package com.somshare.somshare.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS S3 설정
 * - 프로필 이미지, 시험 파일 등을 S3에 저장할 때 사용
 * - dev/prod 프로파일에서만 활성화
 */
@Configuration
@Profile({"simple", "dev", "prod"})
public class AwsS3Config {

    @Value("${aws.s3.access-key:dummy}")
    private String accessKey;

    @Value("${aws.s3.secret-key:dummy}")
    private String secretKey;

    @Value("${aws.region:ap-northeast-2}")
    private String region;

    @Value("${aws.s3.bucket:dummy-bucket}")
    private String bucketName;

    /**
     * S3 클라이언트 생성
     */
    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    /**
     * Presigned URL 발급용 Presigner
     */
    @Bean
    public S3Presigner s3Presigner() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    public String getBucketName() {
        return bucketName;
    }
}
