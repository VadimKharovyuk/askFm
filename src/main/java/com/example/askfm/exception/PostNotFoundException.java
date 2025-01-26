package com.example.askfm.exception;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(Long message) {
        super(String.valueOf(message));
    }
}
