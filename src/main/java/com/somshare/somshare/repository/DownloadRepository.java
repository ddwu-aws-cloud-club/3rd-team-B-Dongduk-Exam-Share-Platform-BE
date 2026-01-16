package com.somshare.somshare.repository;

import com.somshare.somshare.domain.Download;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DownloadRepository extends JpaRepository<Download, Long> {

    Optional<Download> findByUserIdAndPostId(Long userId, Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);
}
