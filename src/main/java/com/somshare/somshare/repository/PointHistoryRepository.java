package com.somshare.somshare.repository;

import com.somshare.somshare.domain.PointHistory;
import com.somshare.somshare.domain.PointType; // PointType import 확인
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    // 내 포인트 내역 전체 조회
    Page<PointHistory> findAllByUserId(Long userId, Pageable pageable);

    // 중복 구매 방지용
    boolean existsByUserIdAndFileIdAndType(Long userId, Long fileId, PointType type);

    // 총 획득 포인트 계산
    @Query("SELECT COALESCE(SUM(ph.amount), 0) FROM PointHistory ph WHERE ph.user.id = :userId AND ph.type = 'EARN'")
    Integer getTotalEarnedPoints(@Param("userId") Long userId);

    // 총 사용 포인트 계산
    @Query("SELECT COALESCE(SUM(ph.amount), 0) FROM PointHistory ph WHERE ph.user.id = :userId AND ph.type = 'REDUCE'")
    Integer getTotalSpentPoints(@Param("userId") Long userId);

    // 총 다운로드 횟수 (포인트 사용 = 다운로드)
    @Query("SELECT COUNT(ph) FROM PointHistory ph WHERE ph.user.id = :userId AND ph.type = 'REDUCE'")
    Long countDownloads(@Param("userId") Long userId);
}