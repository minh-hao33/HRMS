package com.example.hrms.exception;

// Ngoại lệ này được sử dụng khi có yêu cầu không hợp lệ.
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}