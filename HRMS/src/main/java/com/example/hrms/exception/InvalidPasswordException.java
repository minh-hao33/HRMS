package com.example.hrms.exception;

public class InvalidPasswordException extends RuntimeException {
    // Constructor chấp nhận một thông báo
    public InvalidPasswordException(String message) {
        super(message); // Truyền thông báo cho constructor của lớp cha
    }
}