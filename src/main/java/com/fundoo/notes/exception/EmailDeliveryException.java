package com.fundoo.notes.exception;

/* Raised when the SMTP server cannot deliver a password-reset email. */
public class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(String message) {
        super(message);
    }
}
