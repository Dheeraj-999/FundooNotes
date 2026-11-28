package com.fundoo.notes.service.impl;

import com.fundoo.notes.exception.EmailDeliveryException;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmtpMailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private SmtpMailService service;

    @BeforeEach
    void setUp() {
        service = new SmtpMailService(mailSender, "no-reply@fundoo.test", "");
        ReflectionTestUtils.setField(service, "otpExpirationSeconds", 300L);
    }

    @Test
    void sendsTheCodeToTheRequestedAddress() throws Exception {
        service.sendPasswordResetOtp("ada@example.com", "123456");

        ArgumentCaptor<MimeMessagePreparator> captor =
                ArgumentCaptor.forClass(MimeMessagePreparator.class);
        verify(mailSender).send(captor.capture());

        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        captor.getValue().prepare(message);

        assertEquals("Your Fundoo Notes verification code", message.getSubject());
        assertEquals("ada@example.com", message.getRecipients(Message.RecipientType.TO)[0].toString());
        assertTrue(message.getContent().toString().contains("123456"));
        assertTrue(message.getContent().toString().contains("5 minute(s)"));
    }

    @Test
    void wrapsSmtpFailuresInEmailDeliveryException() {
        doThrow(new MailSendException("SMTP down")).when(mailSender).send(any(MimeMessagePreparator.class));

        assertThrows(
                EmailDeliveryException.class,
                () -> service.sendPasswordResetOtp("ada@example.com", "123456"));
    }
}
