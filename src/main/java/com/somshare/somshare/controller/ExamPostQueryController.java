package com.somshare.somshare.controller;

import com.somshare.somshare.dto.ExamPostListResponse;
import com.somshare.somshare.service.ExamPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class ExamPostQueryController {

    private final ExamPostService examPostService;

    /**
     * 전체 게시글 검색/정렬/페이징
     * 예) GET /api/posts?search=자료&major=컴퓨터학전공&page=0&size=20&sort=latest|popular|downloads
     */
    @GetMapping
    public ExamPostListResponse getPosts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String major,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        log.info("[POSTS_LIST_REQ] search={} major={} page={} size={} sort={}", search, major, page, size, sort);
        ExamPostListResponse res = examPostService.getPosts(search, major, page, size, sort);
        log.info("[POSTS_LIST_RES] totalElements={} totalPages={} currentPage={}",
                res.totalElements(), res.totalPages(), res.currentPage());
        return res;
    }
}
