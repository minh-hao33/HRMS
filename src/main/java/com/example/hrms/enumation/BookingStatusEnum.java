package com.example.hrms.enumation;

import lombok.Getter;

@Getter
public enum BookingStatusEnum {
    Requested("Requested"),
    Confirmed("Confirmed"),
    Cancelled("Cancelled");

    private final String value;

    BookingStatusEnum(String value) {
        this.value = value;
    }

}
