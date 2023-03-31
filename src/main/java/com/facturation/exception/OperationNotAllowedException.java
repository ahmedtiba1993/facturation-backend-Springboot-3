package com.facturation.exception;

import lombok.Data;

@Data
public class OperationNotAllowedException extends RuntimeException {

    private ErrorCodes errorCode;

    public OperationNotAllowedException(String message) {
        super(message);
    }
}
