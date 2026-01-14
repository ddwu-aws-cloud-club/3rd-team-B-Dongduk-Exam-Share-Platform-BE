package com.somshare.somshare.controller;

import com.somshare.somshare.dto.MyUploadsPageResponse;
import com.somshare.somshare.service.ExamPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final ExamPostService examPostService;

    @GetMapping("/me/uploads")
    public MyUploadsPageResponse getMyUploads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal
    ) {
        return examPostService.getMyUploads(principal.getName(), page, size);
    }
}
