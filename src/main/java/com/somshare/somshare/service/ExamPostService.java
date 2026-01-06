package com.somshare.somshare.service;

import com.somshare.somshare.dto.ExamPostResponse;
import java.util.List;

public interface ExamPostService {
    List<ExamPostResponse> getExamPostsByDepartment(Long departmentId);
}
