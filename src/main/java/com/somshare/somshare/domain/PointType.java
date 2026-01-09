package com.somshare.somshare.domain;

public enum PointType {
    EARN("적립"),
    REDUCE("사용");

    private String description;

    PointType(String description) {
        this.description = description;
    }
}
