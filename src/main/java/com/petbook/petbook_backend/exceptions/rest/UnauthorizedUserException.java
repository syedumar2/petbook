package com.petbook.petbook_backend.exceptions.rest;

public class UnauthorizedUserException extends RuntimeException {
    public UnauthorizedUserException(String s) {
        super(s);
    }
}
