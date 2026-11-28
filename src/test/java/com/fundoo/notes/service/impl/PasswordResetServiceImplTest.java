package com.fundoo.notes.service.impl;

import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.EmailDeliveryException;
import com.fundoo.notes.exception.InvalidOtpException;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.MailService;
import com.fundoo.notes.service.OtpService;
import com.fundoo.notes.service.PasswordService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    private static final String GENERIC_MESSAGE =
            "If the email is registered, a verification code has been sent.";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private OtpService otpService;

    @Mock
    private MailService mailService;

    @InjectMocks
    private PasswordResetServiceImpl service;

    @Test
    void emailsAnOtpToARegisteredUserWithoutReturningIt() {
        User user = new User("Ada", "ada@example.com", "oldHash");
        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.of(user));
        when(otpService.generateOtp("ada@example.com")).thenReturn("123456");

        String message = service.requestReset("  ADA@Example.com  ");

        assertEquals(GENERIC_MESSAGE, message);
        assertFalse(message.contains("123456"));
        verify(mailService).sendPasswordResetOtp("ada@example.com", "123456");
    }

    @Test
    void givesTheSameResponseForAnUnknownEmailAndSendsNothing() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertEquals(GENERIC_MESSAGE, service.requestReset("missing@example.com"));
        verifyNoInteractions(otpService, mailService);
    }

    @Test
    void propagatesAnEmailDeliveryFailure() {
        when(userRepository.findByEmail("ada@example.com"))
                .thenReturn(Optional.of(new User("Ada", "ada@example.com", "hash")));
        when(otpService.generateOtp("ada@example.com")).thenReturn("123456");
        doThrow(new EmailDeliveryException("SMTP down"))
                .when(mailService).sendPasswordResetOtp("ada@example.com", "123456");

        assertThrows(EmailDeliveryException.class, () -> service.requestReset("ada@example.com"));
    }

    @Test
    void storesANewEncodedPasswordAfterTheOtpIsVerified() {
        User user = new User("Ada", "ada@example.com", "oldHash");
        when(otpService.verifyOtp("ada@example.com", "123456")).thenReturn(true);
        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.of(user));
        when(passwordService.encodePassword("newPassword")).thenReturn("newHash");

        String message = service.resetPassword(" ADA@example.com ", " 123456 ", "newPassword");

        assertEquals("Password updated successfully", message);
        verify(otpService).verifyOtp("ada@example.com", "123456");
        verify(passwordService).encodePassword("newPassword");
        verify(userRepository).save(user);
        assertEquals("newHash", user.getPassword());
    }

    @Test
    void rejectsAnInvalidOtpWithoutChangingThePassword() {
        when(otpService.verifyOtp("ada@example.com", "000000")).thenReturn(false);

        assertThrows(
                InvalidOtpException.class,
                () -> service.resetPassword("ada@example.com", "000000", "newPassword"));
        verify(userRepository, never()).save(any());
        verify(passwordService, never()).encodePassword(anyString());
    }
}
