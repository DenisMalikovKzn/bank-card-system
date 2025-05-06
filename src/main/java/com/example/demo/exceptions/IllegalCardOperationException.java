package com.example.demo.exceptions;

public class IllegalCardOperationException extends RuntimeException {
    public IllegalCardOperationException(String message) {
        super(message);
    }
}
