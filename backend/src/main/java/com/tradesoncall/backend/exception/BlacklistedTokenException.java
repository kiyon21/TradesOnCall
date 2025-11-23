package com.tradesoncall.backend.exception;

public class BlacklistedTokenException extends RuntimeException {
    public BlacklistedTokenException(String message) {
        super(message);
    }
}
