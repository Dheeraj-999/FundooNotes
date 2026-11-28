package com.fundoo.notes.controller;

import com.fundoo.notes.dto.request.ForgotPasswordRequestDto;
import com.fundoo.notes.dto.request.ResetPasswordRequestDto;
import com.fundoo.notes.service.PasswordResetService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetControllerTest {

    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private PasswordResetController controller;

    @Test
    void forgotPasswordReturnsTheServiceMessage() {
        ForgotPasswordRequestDto request = new ForgotPasswordRequestDto();
        request.setEmail("ada@example.com");
        when(passwordResetService.requestReset("ada@example.com")).thenReturn("Code sent");

        ResponseEntity<Map<String, String>> response = controller.forgotPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Code sent", response.getBody().get("message"));
    }

    @Test
    void resetPasswordForwardsAllThreeFields() {
        ResetPasswordRequestDto request = new ResetPasswordRequestDto();
        request.setEmail("ada@example.com");
        request.setOtp("123456");
        request.setNewPassword("newPassword");
        when(passwordResetService.resetPassword("ada@example.com", "123456", "newPassword"))
                .thenReturn("Password updated successfully");

        ResponseEntity<Map<String, String>> response = controller.resetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password updated successfully", response.getBody().get("message"));
        verify(passwordResetService).resetPassword("ada@example.com", "123456", "newPassword");
    }
}
