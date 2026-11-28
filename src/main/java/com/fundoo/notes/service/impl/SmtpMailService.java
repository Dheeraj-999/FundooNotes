package com.fundoo.notes.service.impl;

import com.fundoo.notes.exception.EmailDeliveryException;
import com.fundoo.notes.service.MailService;

import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * Class Name  : SmtpMailService
 * Description : Sends password-reset codes and due reminder notifications over SMTP.
 */
@Service
public class SmtpMailService implements MailService {

    private static final Logger logger = LoggerFactory.getLogger(SmtpMailService.class);
    private static final DateTimeFormatter REMINDER_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm");

    private final JavaMailSender mailSender;
    private final String fromAddress;

    @Value("${app.redis.otp-expiration:300}")
    private long otpExpirationSeconds;

    public SmtpMailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from:}") String configuredFrom,
            @Value("${app.mail.username:}") String username) {

        this.mailSender = mailSender;
        this.fromAddress = configuredFrom.isBlank() ? username : configuredFrom;
    }

    @Override
    public void sendPasswordResetOtp(String email, String otp) {
        long expiryMinutes = Math.max(1, otpExpirationSeconds / 60);
        String subject = "Your Fundoo Notes verification code";
        String body = "Hello,\n\n"
                + "Use this verification code to reset your Fundoo Notes password:\n\n"
                + otp + "\n\n"
                + "It expires in " + expiryMinutes + " minute(s). "
                + "If you did not request this, you can ignore this email.\n\n"
                + "— The Fundoo Notes team";

        sendEmail(email, subject, body);
        logger.info("Password reset code emailed to {}", email);
    }

    @Override
    public void sendReminder(String email, String noteTitle, LocalDateTime reminderTime) {
        String subject = "Reminder: " + noteTitle;
        String body = "Hello,\n\n"
                + "This is a reminder for your Fundoo Notes note:\n\n"
                + noteTitle + "\n\n"
                + "Scheduled for " + reminderTime.format(REMINDER_TIME_FORMAT) + ".\n\n"
                + "— The Fundoo Notes team";

        sendEmail(email, subject, body);
        logger.info("Reminder email sent to {} for note '{}'", email, noteTitle);
    }

    private void sendEmail(String email, String subject, String body) {
        MimeMessagePreparator preparator = message -> {
            if (!fromAddress.isBlank()) {
                message.setFrom(new InternetAddress(fromAddress));
            }
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject(subject);
            message.setText(body);
        };

        try {
            mailSender.send(preparator);
        } catch (MailException exception) {
            logger.error("Failed to email {} to {}", subject, email, exception);
            throw new EmailDeliveryException("Unable to send the email. Please try again later.");
        }
    }
}
