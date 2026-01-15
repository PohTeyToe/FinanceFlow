package com.financeflow.account.exception;

public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException() {
        super("Unauthorized access to resource");
    }

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
