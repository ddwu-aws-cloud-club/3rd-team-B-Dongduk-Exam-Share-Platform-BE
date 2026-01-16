package com.somshare.somshare.repository;

import com.somshare.somshare.domain.PointHistory;
import com.somshare.somshare.domain.PointType; // PointType import 확인
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    // 내 포인트 내역 전체 조회 (타입 필터링 포함)
    @Query("SELECT ph FROM PointHistory ph WHERE ph.user.id = :userId " +
            "AND (:type IS NULL OR ph.type = :type) ORDER BY ph.createdAt DESC")
    Page<PointHistory> findAllByUserIdAndType(@Param("userId") Long userId, @Param("type") PointType type, Pageable pageable);

    // 요약 통계용: 특정 타입들의 합계 계산
    @Query("SELECT COALESCE(SUM(ph.amount), 0) FROM PointHistory ph WHERE ph.user.id = :userId AND ph.amount > 0")
    int sumEarnedPoints(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(ph.amount), 0) FROM PointHistory ph WHERE ph.user.id = :userId AND ph.amount < 0")
    int sumSpentPoints(@Param("userId") Long userId);

    // 중복 구매 방지용
    boolean existsByUserIdAndFileIdAndType(Long userId, Long fileId, PointType type);

    // 총 획득 포인트 계산 (UPLOAD, ADMIN_ADD)
    @Query("SELECT COALESCE(SUM(ph.amount), 0) FROM PointHistory ph WHERE ph.user.id = :userId AND ph.amount > 0")
    Integer getTotalEarnedPoints(@Param("userId") Long userId);

    // 총 사용 포인트 계산 (DOWNLOAD, ADMIN_DEDUCT - 절대값으로 반환)
    @Query("SELECT COALESCE(ABS(SUM(ph.amount)), 0) FROM PointHistory ph WHERE ph.user.id = :userId AND ph.amount < 0")
    Integer getTotalSpentPoints(@Param("userId") Long userId);

    // 총 다운로드 횟수
    @Query("SELECT COUNT(ph) FROM PointHistory ph WHERE ph.user.id = :userId AND ph.type = com.somshare.somshare.domain.PointType.DOWNLOAD")
    Long countDownloads(@Param("userId") Long userId);
}