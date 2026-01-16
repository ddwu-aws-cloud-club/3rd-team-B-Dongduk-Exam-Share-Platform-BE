package com.somshare.somshare.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class MyUploadsPageResponse {
    private List<MyUploadItemResponse> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;

    public static MyUploadsPageResponse from(Page<MyUploadItemResponse> page) {
        return new MyUploadsPageResponse(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber()
        );
    }
}
