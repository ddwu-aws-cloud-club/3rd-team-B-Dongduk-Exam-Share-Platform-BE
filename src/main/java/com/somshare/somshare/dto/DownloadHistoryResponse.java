package com.somshare.somshare.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class DownloadHistoryResponse {

    private List<DownloadItemDto> content;
    private Long totalElements;
    private Integer totalPages;
    private Integer currentPage;

    public static DownloadHistoryResponse from(Page<DownloadItemDto> page) {
        return DownloadHistoryResponse.builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .build();
    }
}
