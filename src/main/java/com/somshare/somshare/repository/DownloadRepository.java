package com.somshare.somshare.repository;

import com.somshare.somshare.domain.Download;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DownloadRepository extends JpaRepository<Download, Long> {

    Optional<Download> findByUserIdAndPostId(Long userId, Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);

    // 사용자가 다운로드한 모든 게시글 ID 조회
    @Query("SELECT d.post.id FROM Download d WHERE d.user.id = :userId")
    List<Long> findPostIdsByUserId(@Param("userId") Long userId);

    // 사용자의 다운로드 내역 조회
    List<Download> findByUserIdOrderByDownloadedAtDesc(Long userId);

    // 사용자의 다운로드 내역 페이징 조회
    Page<Download> findByUserIdOrderByDownloadedAtDesc(Long userId, Pageable pageable);
}
