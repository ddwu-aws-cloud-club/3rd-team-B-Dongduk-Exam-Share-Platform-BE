package com.somshare.somshare.repository;

import com.somshare.somshare.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByUploaderIdOrderByUploadDateDesc(Long uploaderId);

    List<Post> findByMajorOrderByUploadDateDesc(String major);

    List<Post> findAllByOrderByUploadDateDesc();

    // 검색 / 전공 필터 / 페이지네이션
    @Query("""
        select p
        from Post p
        where (:major is null or :major = '' or p.major = :major)
          and (
                :search is null or :search = '' or
                lower(p.title) like lower(concat('%', :search, '%')) or
                lower(coalesce(p.subject, '')) like lower(concat('%', :search, '%')) or
                lower(coalesce(p.professor, '')) like lower(concat('%', :search, '%'))
          )
        """)
    Page<Post> searchPosts(
            @Param("major") String major,
            @Param("search") String search,
            Pageable pageable
    );
}
