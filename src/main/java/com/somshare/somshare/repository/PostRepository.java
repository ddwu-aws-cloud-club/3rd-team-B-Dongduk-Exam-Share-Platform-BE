package com.somshare.somshare.repository;

import com.somshare.somshare.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByUploaderIdOrderByUploadDateDesc(Long uploaderId);

    List<Post> findByMajorOrderByUploadDateDesc(String major);

    List<Post> findAllByOrderByUploadDateDesc();
}
