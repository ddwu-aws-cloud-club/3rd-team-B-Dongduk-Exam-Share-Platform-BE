package com.somshare.somshare.service;

import com.somshare.somshare.domain.Department;
import com.somshare.somshare.domain.ExamPost;
import com.somshare.somshare.domain.User;
import com.somshare.somshare.dto.*;
import com.somshare.somshare.repository.DepartmentRepository;
import com.somshare.somshare.repository.ExamPostRepository;
import com.somshare.somshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
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
    public ExamPostListResponse getPosts(String search, String major, Long departmentId, int page, int size, String sort) {
        String departmentName = parseDepartmentNameOrNull(major);

        Pageable pageable = PageRequest.of(page, size, toSort(sort));

        Page<ExamPost> result = (departmentId != null)
                ? examPostRepository.searchPostsByDepartmentId(departmentId, departmentName, search, pageable)
                : examPostRepository.searchPosts(departmentName, search, pageable);

        List<ExamPostSummaryResponse> content = result.getContent().stream()
                .map(this::toSummaryResponse)
                .toList();

        return new ExamPostListResponse(
                content,
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    private String parseDepartmentNameOrNull(String major) {
        if (major == null || major.isBlank() || major.equalsIgnoreCase("all")) {
            return null;
        }
        return major;
    }

    private Sort toSort(String sort) {
        if (sort == null || sort.isBlank()) sort = "latest";

        return switch (sort.toLowerCase()) {
            case "popular" ->
                    Sort.by(Sort.Order.desc("points"), Sort.Order.desc("createdAt"));
            case "downloads" ->
                    Sort.by(Sort.Order.desc("downloadCount"), Sort.Order.desc("createdAt"));
            default ->
                    Sort.by(Sort.Order.desc("createdAt"));
        };
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
                request.subject(),
                request.professor(),
                uploader,
                dept,
                request.fileKey(),
                request.fileUrl()
        );

        return toResponse(examPostRepository.save(post));
    }

    @Override
    @Transactional
    public ExamPostResponse updateExamPost(Long departmentId, Long postId, ExamPostUpdateRequest request, String username) {
        if (username == null) throw new SecurityException("로그인이 필요합니다.");

        User requester = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ExamPost post = examPostRepository.findByIdAndDepartment_Id(postId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getUploader().getId().equals(requester.getId())) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        post.update(
                request.title() != null ? request.title() : post.getTitle(),
                request.content() != null ? request.content() : post.getContent(),
                request.subject() != null ? request.subject() : post.getSubject(),
                request.professor() != null ? request.professor() : post.getProfessor()
        );

        if (request.fileKey() != null || request.fileUrl() != null) {
            post.updateFile(
                    request.fileKey() != null ? request.fileKey() : post.getFileKey(),
                    request.fileUrl() != null ? request.fileUrl() : post.getFileUrl()
            );
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

        if (!post.getUploader().getId().equals(requester.getId())) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        String fileKey = post.getFileKey();
        if (fileKey != null && !fileKey.isBlank()) {
            s3FileStorageService.deleteFile(fileKey);
        }

        examPostRepository.delete(post);
    }

    @Override
    public MyUploadsPageResponse getMyUploads(String username, int page, int size) {
        if (username == null) throw new SecurityException("로그인이 필요합니다.");

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<MyUploadItemResponse> mapped = examPostRepository
                .findByUploaderEmail(username, pageable)
                .map(MyUploadItemResponse::from);

        return MyUploadsPageResponse.from(mapped);
    }

    private ExamPostResponse toResponse(ExamPost post) {
        return new ExamPostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getSubject(),
                post.getProfessor(),
                post.getUploader().getId(),
                post.getDepartment().getId(),
                post.getFileKey(),
                post.getFileUrl(),
                post.getPoints(),
                post.getDownloadCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    private ExamPostSummaryResponse toSummaryResponse(ExamPost post) {
        String deptName = post.getDepartment().getName();

        return ExamPostSummaryResponse.from(
                post.getId(),
                post.getTitle(),
                post.getSubject(),
                post.getProfessor(),
                deptName,
                deptName,
                post.getCreatedAt(),
                null,
                post.getPoints(),
                post.getDownloadCount()
        );
    }
}
