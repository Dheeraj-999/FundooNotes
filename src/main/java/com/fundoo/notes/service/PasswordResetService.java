package com.fundoo.notes.service;

/* Password recovery workflow: email a code, then exchange it for a new password. */
public interface PasswordResetService {

    String requestReset(String email);

    String resetPassword(String email, String otp, String newPassword);
}
