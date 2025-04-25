package com.example.hrms.exception;

// Ngoại lệ này được sử dụng khi một tài nguyên không được tìm thấy.
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}