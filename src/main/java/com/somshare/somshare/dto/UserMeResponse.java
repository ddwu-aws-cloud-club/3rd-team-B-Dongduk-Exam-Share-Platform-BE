package com.somshare.somshare.dto;

import com.somshare.somshare.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserMeResponse {

    private Long id;
    private String email;
    private String nickname;
    private String college;
    private String major;
    private String majorCode;
    private Integer points;
    private String profileImage;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private Long totalUploads;
    private Long totalDownloads;
    private Integer totalEarnedPoints;
    private Integer totalSpentPoints;

    public static UserMeResponse of(User user, Long totalUploads, Long totalDownloads,
                                     Integer totalEarnedPoints, Integer totalSpentPoints) {
        return UserMeResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .college(user.getCollege())
                .major(user.getMajor())
                .majorCode(generateMajorCode(user.getMajor())) // majorCode 생성
                .points(user.getPoints())
                .profileImage(user.getProfileImageUrl())
                .isVerified(user.getIsVerified())
                .createdAt(user.getCreatedAt())
                .totalUploads(totalUploads)
                .totalDownloads(totalDownloads)
                .totalEarnedPoints(totalEarnedPoints)
                .totalSpentPoints(totalSpentPoints)
                .build();
    }

    // 전공명을 majorCode로 변환하는 헬퍼 메서드
    private static String generateMajorCode(String major) {
        if (major == null) {
            return null;
        }
        // 간단한 예시 매핑 (실제로는 DB에 저장하거나 더 정교한 로직 필요)
        switch (major) {
            case "컴퓨터학전공":
                return "computer-science";
            case "정보통계학과":
                return "information-statistics";
            case "데이터사이언스학과":
                return "data-science";
            case "사이버보안전공":
                return "cyber-security";
            default:
                // 기본적으로 한글을 영문으로 변환 (간단한 예시)
                return major.toLowerCase().replaceAll("\\s+", "-");
        }
    }
}
