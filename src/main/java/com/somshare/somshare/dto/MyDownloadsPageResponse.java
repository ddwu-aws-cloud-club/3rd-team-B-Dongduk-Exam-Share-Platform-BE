package com.somshare.somshare.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class MyDownloadsPageResponse {
    private List<MyDownloadItemResponse> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;

    public static MyDownloadsPageResponse from(Page<MyDownloadItemResponse> page) {
        return new MyDownloadsPageResponse(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber()
        );
    }
}
