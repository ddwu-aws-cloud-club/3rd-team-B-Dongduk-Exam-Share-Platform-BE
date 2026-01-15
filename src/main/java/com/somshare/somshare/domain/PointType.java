package com.somshare.somshare.domain;

public enum PointType {
    UPLOAD("업로드 - 적립"),
    DOWNLOAD("다운로드 - 사용"),
    ADMIN_ADD("관리자 지급"),
    ADMIN_DEDUCT("관리자 차감"),
    ALL("조회용");

    private String description;

    PointType(String description) {
        this.description = description;
    }
}
