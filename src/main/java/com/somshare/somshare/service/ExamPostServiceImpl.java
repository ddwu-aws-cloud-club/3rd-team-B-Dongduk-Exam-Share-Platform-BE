package com.somshare.somshare.service;

import com.somshare.somshare.domain.Department;
import com.somshare.somshare.domain.ExamPost;
import com.somshare.somshare.domain.User;
import com.somshare.somshare.dto.ExamPostCreateRequest;
import com.somshare.somshare.dto.ExamPostResponse;
import com.somshare.somshare.dto.ExamPostUpdateRequest;
import com.somshare.somshare.repository.DepartmentRepository;
import com.somshare.somshare.repository.ExamPostRepository;
import com.somshare.somshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamPostServiceImpl implements ExamPostService {

    private final ExamPostRepository examPostRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final S3FileStorageService s3FileStorageService;

    @Override
    public List<ExamPostResponse> getExamPostsByDepartment(Long departmentId) {
        return examPostRepository.findByDepartment_IdOrderByCreatedAtDesc(departmentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ExamPostResponse getExamPostDetail(Long departmentId, Long postId) {
        ExamPost post = examPostRepository.findByIdAndDepartment_Id(postId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        return toResponse(post);
    }

    @Override
    @Transactional
    public ExamPostResponse createExamPost(Long departmentId, ExamPostCreateRequest request, String username) {
        if (username == null) throw new SecurityException("로그인이 필요합니다.");

        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("학과를 찾을 수 없습니다."));

        User uploader = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ExamPost post = new ExamPost(
                request.title(),
                request.content(),
                uploader,
                dept,
                request.fileKey(),
                request.fileUrl()
        );

        ExamPost saved = examPostRepository.save(post);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ExamPostResponse updateExamPost(Long departmentId, Long postId, ExamPostUpdateRequest request, String username) {
        if (username == null) throw new SecurityException("로그인이 필요합니다.");

        User requester = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ExamPost post = examPostRepository.findByIdAndDepartment_Id(postId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 작성자만 수정
        if (!post.getUploader().getId().equals(requester.getId())) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        String newTitle = (request.title() != null) ? request.title() : post.getTitle();
        String newContent = (request.content() != null) ? request.content() : post.getContent();
        post.update(newTitle, newContent);

        if (request.fileKey() != null || request.fileUrl() != null) {
            String newFileKey = (request.fileKey() != null) ? request.fileKey() : post.getFileKey();
            String newFileUrl = (request.fileUrl() != null) ? request.fileUrl() : post.getFileUrl();
            post.updateFile(newFileKey, newFileUrl);
        }

        return toResponse(post);
    }

    @Override
    @Transactional
    public void deleteExamPost(Long departmentId, Long postId, String username) {
        if (username == null) throw new SecurityException("로그인이 필요합니다.");

        User requester = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ExamPost post = examPostRepository.findByIdAndDepartment_Id(postId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 작성자만 삭제
        if (!post.getUploader().getId().equals(requester.getId())) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        // S3에서 파일 삭제
        String fileKey = post.getFileKey();
        if (fileKey != null && !fileKey.isBlank()) {
            s3FileStorageService.deleteFile(fileKey);
        }

        examPostRepository.delete(post);
    }

    private ExamPostResponse toResponse(ExamPost post) {
        return new ExamPostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUploader().getId(),
                post.getDepartment().getId(),
                post.getFileKey(),
                post.getFileUrl(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
