package com.petbook.petbook_backend.exceptions.rest;

public class UserNotFoundException  extends RuntimeException{
    public UserNotFoundException(String message){
        super(message);
    }
}
