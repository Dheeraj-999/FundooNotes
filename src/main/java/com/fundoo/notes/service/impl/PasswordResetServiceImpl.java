package com.fundoo.notes.service.impl;

import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.EmailDeliveryException;
import com.fundoo.notes.exception.InvalidOtpException;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.MailService;
import com.fundoo.notes.service.OtpService;
import com.fundoo.notes.service.PasswordResetService;
import com.fundoo.notes.service.PasswordService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/*
 * Class Name  : PasswordResetServiceImpl
 * Description : Generates and emails an OTP, then validates it before storing a
 *               freshly encoded password. Unknown emails get the same response as
 *               known ones so the endpoint cannot be used to enumerate accounts.
 */
@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetServiceImpl.class);
    private static final String GENERIC_MESSAGE =
            "If the email is registered, a verification code has been sent.";

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final OtpService otpService;
    private final MailService mailService;

    public PasswordResetServiceImpl(
            UserRepository userRepository,
            PasswordService passwordService,
            OtpService otpService,
            MailService mailService) {

        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.otpService = otpService;
        this.mailService = mailService;
    }

    @Override
    public String requestReset(String email) {
        String normalizedEmail = normalize(email);
        Optional<User> existingUser = userRepository.findByEmail(normalizedEmail);

        if (existingUser.isPresent()) {
            String otp = otpService.generateOtp(normalizedEmail);
            try {
                mailService.sendPasswordResetOtp(normalizedEmail, otp);
            } catch (EmailDeliveryException exception) {
                logger.error("Password reset email failed for {}", normalizedEmail, exception);
                throw exception;
            }
        } else {
            logger.info("Password reset requested for an unregistered email");
        }

        return GENERIC_MESSAGE;
    }

    @Override
    @Transactional
    public String resetPassword(String email, String otp, String newPassword) {
        String normalizedEmail = normalize(email);
        boolean isValid = otpService.verifyOtp(normalizedEmail, otp.trim());

        if (!isValid) {
            throw new InvalidOtpException("Invalid or expired verification code");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidOtpException("Invalid or expired verification code"));

        user.setPassword(passwordService.encodePassword(newPassword));
        userRepository.save(user);
        logger.info("Password updated for user ID {}", user.getId());

        return "Password updated successfully";
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
