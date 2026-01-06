package com.somshare.somshare.service;

import com.somshare.somshare.dto.ExamPostResponse;
import com.somshare.somshare.repository.ExamPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamPostServiceImpl implements ExamPostService {

    private final ExamPostRepository examPostRepository;

    @Override
    public List<ExamPostResponse> getExamPostsByDepartment(Long departmentId) {
        return examPostRepository.findByDepartment_IdOrderByCreatedAtDesc(departmentId)
                .stream()
                .map(ExamPostResponse::from)
                .toList();
    }
}
