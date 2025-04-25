package com.example.hrms.enumation;

import lombok.Getter;

@Getter
public enum RoleEnum {
    EMPLOYEE("EMPLOYEE"),
    SUPERVISOR("SUPERVISOR"),
    ADMIN("ADMIN");

    private final String value;

    RoleEnum(String value) {
        this.value = value;
    }

    public static RoleEnum fromString(String value) {
        for (RoleEnum role : values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}