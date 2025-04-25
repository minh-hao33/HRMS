package com.example.hrms.enumation;

import lombok.Getter;

@Getter
public enum RequestStatusEnum {
    PENDING("Pending"),   // Thêm trạng thái PENDING
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String value;

    RequestStatusEnum(String value) {
        this.value = value;
    }
}