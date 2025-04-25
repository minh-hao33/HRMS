package com.example.hrms.exception;

public class InvalidArgumentException extends RuntimeException {

    public InvalidArgumentException() {
        super("INVALID_ARGUMENT");
    }

    public InvalidArgumentException(String message) {
        super(message);
    }

    public InvalidArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}


