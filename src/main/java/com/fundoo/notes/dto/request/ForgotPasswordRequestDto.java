package com.fundoo.notes.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/*
 * Class Name  : ForgotPasswordRequestDto
 * Description : Validates the email submitted by a user who forgot their password.
 */

public class ForgotPasswordRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    public ForgotPasswordRequestDto() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
