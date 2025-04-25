package com.example.hrms.enumation;

import lombok.Getter;

@Getter
public enum DepartmentEnum {
    HR("HR"),
    FINANCE("Finance"),
    IT("IT");

    private final String value;

    DepartmentEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value; // Trả về giá trị tương ứng
    }
}