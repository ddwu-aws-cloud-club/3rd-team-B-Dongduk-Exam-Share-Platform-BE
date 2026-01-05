package com.somshare.somshare.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 설정
 * - 프로필 이미지, 시험 파일 등을 S3에 저장할 때 사용
 * - 현재는 로컬 파일 시스템 사용 중 (LocalFileStorageService)
 * - 나중에 S3로 전환할 때 이 설정을 활성화하면 됨
 */
@Configuration
public class AwsS3Config {

    @Value("${aws.s3.access-key:}")
    private String accessKey;

    @Value("${aws.s3.secret-key:}")
    private String secretKey;

    @Value("${aws.region:ap-northeast-2}")
    private String region;

    @Value("${aws.s3.bucket:}")
    private String bucketName;

    /**
     * S3 클라이언트 생성
     * - 주석 처리: 현재는 사용하지 않음 (로컬 파일 저장)
     * - S3 사용 시 @Bean 주석 해제하고 LocalFileStorageService를 S3FileStorageService로 교체
     */
    // @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    public String getBucketName() {
        return bucketName;
    }
}
