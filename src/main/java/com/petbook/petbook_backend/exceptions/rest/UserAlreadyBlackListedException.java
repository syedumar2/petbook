package com.petbook.petbook_backend.exceptions.rest;

public class UserAlreadyBlackListedException extends RuntimeException {
    public UserAlreadyBlackListedException(String message) {
        super(message);
    }
}
