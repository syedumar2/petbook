package com.petbook.petbook_backend.exceptions.rest;

public class ImageUploadException extends RuntimeException {
    public ImageUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
