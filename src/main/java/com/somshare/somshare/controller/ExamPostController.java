package com.somshare.somshare.controller;

import com.somshare.somshare.dto.ExamPostResponse;
import com.somshare.somshare.service.ExamPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/departments")
public class ExamPostController {

    private final ExamPostService examPostService;

    @GetMapping("/{departmentId}/exam-posts")
    public List<ExamPostResponse> getExamPosts(@PathVariable Long departmentId) {
        return examPostService.getExamPostsByDepartment(departmentId);
    }
}
