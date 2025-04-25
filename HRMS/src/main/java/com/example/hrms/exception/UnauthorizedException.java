package com.example.hrms.exception;

//Ngoại lệ này được sử dụng khi người dùng không được ủy quyền để thực hiện hành động.
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}