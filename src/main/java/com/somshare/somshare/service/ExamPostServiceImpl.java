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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamPostServiceImpl implements ExamPostService {

    private final ExamPostRepository examPostRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    // ✅ 주입은 유지 (나중에 S3 다시 켤 때 그대로 사용)
    private final S3FileStorageService s3FileStorageService;

    // ✅ 로컬에서만 S3를 끄고 싶으면 true로
    private static final boolean DISABLE_S3_FOR_NOW = true;

    @Override
    public List<ExamPostResponse> getExamPostsByDepartment(Long departmentId) {
        return examPostRepository.findByDepartment_IdOrderByCreatedAtDesc(departmentId)
                .stream()
                .map(ExamPostResponse::from)
                .toList();
    }

    @Override
    public ExamPostResponse getExamPostDetail(Long departmentId, Long postId) {
        return examPostRepository.findByIdAndDepartment_Id(postId, departmentId)
                .map(ExamPostResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("ExamPost not found"));
    }

    @Transactional
    public ExamPostResponse createExamPost(Long departmentId,
                                           ExamPostCreateRequest request,
                                           MultipartFile pdf,
                                           String username) throws IOException {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        User uploader = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String fileKey = null;
        String fileUrl = null;

        if (pdf != null && !pdf.isEmpty()) {
            var stored = s3FileStorageService.storePdf(pdf);
            fileKey = stored.storedName();
            fileUrl = stored.url();
        }

        ExamPost post = new ExamPost(
                request.getTitle(),
                request.getContent(),
                uploader,
                department,
                fileKey,
                fileUrl
        );

        return ExamPostResponse.from(examPostRepository.save(post));
    }


    @Override
    @Transactional
    public void deleteExamPost(Long departmentId, Long postId) {
        ExamPost post = examPostRepository.findByIdAndDepartment_Id(postId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("ExamPost not found"));

        // (선택) S3 삭제는 나중에 S3 켜질 때
        // if (!DISABLE_S3_FOR_NOW && post.getFileKey() != null) s3FileStorageService.delete(post.getFileKey());

        examPostRepository.delete(post);
    }

    @Override
    @Transactional
    public ExamPostResponse updateExamPost(Long departmentId, Long postId, ExamPostUpdateRequest request, MultipartFile pdf) throws IOException {

        ExamPost post = examPostRepository.findByIdAndDepartment_Id(postId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("ExamPost not found"));

        post.update(request.getTitle(), request.getContent());

        if (pdf != null && !pdf.isEmpty()) {
            if (DISABLE_S3_FOR_NOW) {
                String newKey = "mock/pdfs/" + UUID.randomUUID() + "-" + pdf.getOriginalFilename();
                String newUrl = "LOCAL-MOCK://" + pdf.getOriginalFilename();
                post.updateFile(newKey, newUrl);
            } else {
                String oldKey = post.getFileKey();

                S3FileStorageService.StoredFile stored = s3FileStorageService.storePdf(pdf);
                post.updateFile(stored.storedName(), stored.url());

                // (선택) if (oldKey != null) s3FileStorageService.delete(oldKey);
            }
        }

        return ExamPostResponse.from(post);
    }
}
