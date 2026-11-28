package com.fundoo.notes.service;

/* Abstraction over the SMTP transport so password reset logic stays testable. */
public interface MailService {

    void sendPasswordResetOtp(String email, String otp);
}
