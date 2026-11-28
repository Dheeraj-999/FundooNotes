package com.fundoo.notes.exception;

/* Raised when a password-reset verification code is wrong, expired or reused. */
public class InvalidOtpException extends RuntimeException {

    public InvalidOtpException(String message) {
        super(message);
    }
}
