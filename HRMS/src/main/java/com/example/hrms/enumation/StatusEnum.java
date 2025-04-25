package com.example.hrms.enumation;

import lombok.Getter;

@Getter
public enum StatusEnum {
    ACTIVE("Active"),
    INACTIVE("Inactive");

    private final String value;

    StatusEnum(String value) {
        this.value = value;
    }

}