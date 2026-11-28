package com.fundoo.notes.service;

import java.time.LocalDateTime;

/* Abstraction over the SMTP transport so mail logic stays testable. */
public interface MailService {

    void sendPasswordResetOtp(String email, String otp);

    void sendReminder(String email, String noteTitle, LocalDateTime reminderTime);
}
