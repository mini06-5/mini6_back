package com.aivle.bookapp.exception;

public class TokenUnauthorizedException extends RuntimeException {
    public TokenUnauthorizedException(String message) {
        super(message);
    }
}
