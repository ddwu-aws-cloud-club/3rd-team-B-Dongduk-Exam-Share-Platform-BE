package com.somshare.somshare.service;

import com.somshare.somshare.config.AwsS3Config;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3DownloadService {

    private final S3Presigner s3Presigner;
    private final AwsS3Config awsS3Config;

    /**
     * 다운로드용 Presigned URL 생성 (유효기간 10분)
     */
    public String generatePresignedGetUrl(String key) {
        String bucketName = awsS3Config.getBucketName();

        // 요청 정보 생성
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // 링크 유효기간 10분
                .getObjectRequest(b -> b.bucket(bucketName).key(key))
                .build();

        // URL 발급
        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        // 문자열로 반환
        return presignedRequest.url().toString();
    }
}