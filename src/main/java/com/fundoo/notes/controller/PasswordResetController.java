package com.fundoo.notes.controller;

import com.fundoo.notes.dto.request.ForgotPasswordRequestDto;
import com.fundoo.notes.dto.request.ResetPasswordRequestDto;
import com.fundoo.notes.service.PasswordResetService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/*
 * Class Name  : PasswordResetController
 * Description : Public endpoints for requesting and completing a password reset.
 */
@RestController
@RequestMapping("/api/users")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto request) {

        return ResponseEntity.ok(
                Map.of("message", passwordResetService.requestReset(request.getEmail())));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request) {

        return ResponseEntity.ok(Map.of(
                "message",
                passwordResetService.resetPassword(
                        request.getEmail(), request.getOtp(), request.getNewPassword())));
    }
}
