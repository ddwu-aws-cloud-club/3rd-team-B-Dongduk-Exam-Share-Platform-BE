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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamPostServiceImpl implements ExamPostService {

    private final ExamPostRepository examPostRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    // ✅ 추가
    private final S3FileStorageService s3FileStorageService;

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

    // ✅ 시그니처 변경 + S3 업로드
    @Override
    @Transactional
    public ExamPostResponse createExamPost(Long departmentId, ExamPostCreateRequest request, MultipartFile pdf) throws IOException {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        User uploader = userRepository.findById(request.getUploaderId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String fileKey = null;
        String fileUrl = null;

        if (pdf != null && !pdf.isEmpty()) {
            S3FileStorageService.StoredFile stored = s3FileStorageService.storePdf(pdf);
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

        ExamPost saved = examPostRepository.save(post);
        return ExamPostResponse.from(saved);
    }

    @Override
    @Transactional
    public void deleteExamPost(Long departmentId, Long postId) {
        ExamPost post = examPostRepository.findByIdAndDepartment_Id(postId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("ExamPost not found"));

        // (선택) S3 삭제까지 하려면 아래 E) 추가 후 사용
        // if (post.getFileKey() != null) s3FileStorageService.delete(post.getFileKey());

        examPostRepository.delete(post);
    }

    // ✅ 시그니처 변경 + pdf 오면 교체
    @Override
    @Transactional
    public ExamPostResponse updateExamPost(Long departmentId, Long postId, ExamPostUpdateRequest request, MultipartFile pdf) throws IOException {

        ExamPost post = examPostRepository.findByIdAndDepartment_Id(postId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("ExamPost not found"));

        post.update(request.getTitle(), request.getContent());

        if (pdf != null && !pdf.isEmpty()) {
            String oldKey = post.getFileKey();

            S3FileStorageService.StoredFile stored = s3FileStorageService.storePdf(pdf);
            post.updateFile(stored.storedName(), stored.url());

            // (선택) S3 삭제까지 하려면 아래 E) 추가 후 사용
            // if (oldKey != null) s3FileStorageService.delete(oldKey);
        }

        return ExamPostResponse.from(post);
    }
}
