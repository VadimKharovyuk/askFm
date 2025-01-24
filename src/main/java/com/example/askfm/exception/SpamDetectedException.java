package com.example.askfm.exception;

public class SpamDetectedException extends RuntimeException {
  public SpamDetectedException(String message) {
    super(message);
  }
}
