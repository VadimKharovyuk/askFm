package com.example.askfm.exception;

public class AlreadyRepostedException extends RuntimeException {
    public AlreadyRepostedException(String message) {
        super(message);
    }
}
