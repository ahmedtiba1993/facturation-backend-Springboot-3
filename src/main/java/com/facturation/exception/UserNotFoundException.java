package com.facturation.exception;

import lombok.Getter;

import java.util.List;

public class UserNotFoundException extends RuntimeException{

    @Getter
    private ErrorCodes errorCode;

    @Getter
    private List<String> errors;

    public UserNotFoundException(String message) {
        super(message);
    }
}
