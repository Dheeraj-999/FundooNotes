package com.fundoo.notes.controller;

/*
 * Class Name  : UserController
 * Description : Exposes REST endpoints for user registration and JWT authentication.
 * Author      : Dheeraj Buchha * Date        : 27/11/2026
 */

import com.fundoo.notes.dto.request.LoginRequestDto;
import com.fundoo.notes.dto.request.UserRegisterRequestDto;
import com.fundoo.notes.dto.response.LoginResponseDto;
import com.fundoo.notes.dto.response.UserResponseDto;
import com.fundoo.notes.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new user account with encrypted credentials.
     *
     * @param registrationRequest validated user registration parameters
     * @return HTTP 201 Created with saved user details
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(
            @Valid @RequestBody UserRegisterRequestDto registrationRequest) {

        UserResponseDto registeredUser = userService.register(registrationRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registeredUser);
    }

    /**
     * Authenticates user credentials and issues a signed JSON Web Token (JWT).
     *
     * @param loginRequest validated email and raw password
     * @return HTTP 200 OK with JWT token and success status
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto loginRequest) {

        LoginResponseDto loginResponse = userService.login(loginRequest);

        return ResponseEntity.ok(loginResponse);
    }
}
