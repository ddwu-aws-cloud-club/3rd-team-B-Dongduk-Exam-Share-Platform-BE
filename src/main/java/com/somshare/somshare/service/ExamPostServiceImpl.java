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
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

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

    @Override
    @Transactional
    public ExamPostResponse createExamPost(Long departmentId, ExamPostCreateRequest request, String username) {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        User uploader = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ExamPost post = new ExamPost(
                request.title(),
                request.content(),
                uploader,
                department,
                request.fileKey(),
                request.fileUrl()
        );

        return ExamPostResponse.from(examPostRepository.save(post));
    }

    @Override
    @Transactional
    public ExamPostResponse updateExamPost(Long departmentId, Long postId, ExamPostUpdateRequest request) {

        ExamPost post = examPostRepository.findByIdAndDepartment_Id(postId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("ExamPost not found"));

        post.update(request.title(), request.content());

        // ✅ 파일 교체도 업로드 결과값만 저장
        if (request.fileKey() != null || request.fileUrl() != null) {
            post.updateFile(request.fileKey(), request.fileUrl());
        }

        return ExamPostResponse.from(post);
    }

    @Override
    @Transactional
    public void deleteExamPost(Long departmentId, Long postId) {
        ExamPost post = examPostRepository.findByIdAndDepartment_Id(postId, departmentId)
                .orElseThrow(() -> new IllegalArgumentException("ExamPost not found"));
        examPostRepository.delete(post);
    }
}
