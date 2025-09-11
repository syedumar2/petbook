package com.petbook.petbook_backend.exceptions.rest;

public class ConversationAlreadyExistsException extends RuntimeException {
    public ConversationAlreadyExistsException(String message) {
        super(message);
    }
}
