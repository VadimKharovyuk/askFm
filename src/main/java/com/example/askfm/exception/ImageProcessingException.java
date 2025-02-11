package com.example.askfm.exception;

import java.io.IOException;

public class ImageProcessingException extends RuntimeException {
    public ImageProcessingException(String message, IOException e) {
        super(message);
    }

}
