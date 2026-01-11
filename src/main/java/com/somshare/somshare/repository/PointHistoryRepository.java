package com.somshare.somshare.repository;

import com.somshare.somshare.domain.PointHistory;
import com.somshare.somshare.domain.PointType; // PointType import 확인
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    // 내 포인트 내역 전체 조회
    Page<PointHistory> findAllByUserId(Long userId, Pageable pageable);

    // 중복 구매 방지용
    boolean existsByUserIdAndFileIdAndType(Long userId, Long fileId, PointType type);
}