package com.somshare.somshare.service;

import com.somshare.somshare.domain.Post;
import com.somshare.somshare.domain.User;
import com.somshare.somshare.dto.PostListResponse;
import com.somshare.somshare.dto.PostUploadResponse;
import com.somshare.somshare.repository.DownloadRepository;
import com.somshare.somshare.repository.PostRepository;
import com.somshare.somshare.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private static final int UPLOAD_REWARD_POINTS = 100;
    private static final int DOWNLOAD_COST = 50;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final DownloadRepository downloadRepository;
    private final S3FileStorageService s3FileStorageService;

    @Transactional
    public PostUploadResponse uploadPost(
            MultipartFile file,
            String title,
            String subject,
            String professor,
            String major,
            String username
    ) throws IOException {
        // 인증 필수
        if (username == null) {
            throw new SecurityException("로그인이 필요합니다.");
        }

        User uploader = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 1. S3에 파일 업로드
        S3FileStorageService.StoredFile storedFile = s3FileStorageService.storePdf(file);

        // 2. Post 엔티티 생성 및 저장
        Post post = new Post(
                title,
                subject,
                professor,
                major,
                storedFile.url(),
                storedFile.storedName(),
                uploader
        );
        Post savedPost = postRepository.save(post);

        // 3. 포인트 적립
        uploader.addPoints(UPLOAD_REWARD_POINTS);

        log.info("[POST_UPLOAD_OK] userId={} postId={} earnedPoints={}",
                uploader.getId(), savedPost.getId(), UPLOAD_REWARD_POINTS);

        return PostUploadResponse.from(
                savedPost,
                UPLOAD_REWARD_POINTS,
                "족보가 성공적으로 업로드되었습니다. " + UPLOAD_REWARD_POINTS + "P가 적립되었습니다."
        );
    }

    public PostListResponse getPosts(String search, String major, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadDate"));

        Page<Post> postPage = postRepository.searchPosts(major, search, pageRequest);

        var content = postPage.getContent().stream()
                .map(post -> PostListResponse.PostSummary.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .subject(post.getSubject())
                        .professor(post.getProfessor())
                        .major(post.getMajor())
                        .uploadDate(post.getUploadDate().toLocalDate().toString())
                        .uploaderNickname(post.getUploader().getNickname() != null
                                ? post.getUploader().getNickname()
                                : "익명")
                        .downloadCount((int) downloadRepository.countByPostId(post.getId()))
                        .points(DOWNLOAD_COST)
                        .build())
                .toList();

        return PostListResponse.builder()
                .content(content)
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .currentPage(postPage.getNumber())
                .build();
    }
}
