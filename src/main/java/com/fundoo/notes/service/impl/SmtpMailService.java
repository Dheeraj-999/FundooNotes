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

/*
 * Class Name  : SmtpMailService
 * Description : Sends the password-reset verification code over SMTP.
 */
@Service
public class SmtpMailService implements MailService {

    private static final Logger logger = LoggerFactory.getLogger(SmtpMailService.class);

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
            logger.info("Password reset code emailed to {}", email);
        } catch (MailException exception) {
            logger.error("Failed to email password reset code to {}", email, exception);
            throw new EmailDeliveryException("Unable to send the verification email. Please try again later.");
        }
    }
}
