package com.example.demo.exceptions;

public class UserHasCardsException extends RuntimeException {
    public UserHasCardsException(String message) {
        super(message);
    }
}
