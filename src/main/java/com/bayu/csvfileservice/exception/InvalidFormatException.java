package com.bayu.csvfileservice.exception;

public class InvalidFormatException extends RuntimeException {

    public InvalidFormatException() {
    }

    public InvalidFormatException(String message) {
        super(message);
    }

    public InvalidFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
