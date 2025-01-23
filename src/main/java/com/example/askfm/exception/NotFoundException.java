package com.example.askfm.exception;



public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}