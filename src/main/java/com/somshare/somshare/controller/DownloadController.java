package com.somshare.somshare.controller;

import com.somshare.somshare.dto.DownloadResponse;
import com.somshare.somshare.service.DownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class DownloadController {

    private final DownloadService downloadService;

    @PostMapping("/{postId}/download")
    public ResponseEntity<DownloadResponse> downloadPost(
            @PathVariable Long postId,
            Principal principal
    ) {
        String username = principal.getName();

        log.info("[DOWNLOAD_CONTROLLER] postId={} username={}", postId, username);

        DownloadResponse response = downloadService.downloadPost(postId, username);

        return ResponseEntity.ok(response);
    }
}
