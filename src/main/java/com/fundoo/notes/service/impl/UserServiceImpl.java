package com.fundoo.notes.service.impl;

/*
 * Class Name  : UserServiceImpl
 * Description : Core service implementation managing user lifecycle, credential
 *               encryption with BCrypt, asynchronous registration event dispatching,
 *               and secure JWT generation.
 * Author      : Dheeraj Buchha
 * Note        : Refactored and maintained for FundooNotes (Original: Utkarsh Jain)
 * Date        : 27/11/2026
 */

import com.fundoo.notes.dto.request.LoginRequestDto;
import com.fundoo.notes.dto.request.UserRegisterRequestDto;
import com.fundoo.notes.dto.response.LoginResponseDto;
import com.fundoo.notes.dto.response.UserResponseDto;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.event.UserRegistrationEvent;
import com.fundoo.notes.exception.DuplicateEmailException;
import com.fundoo.notes.exception.InvalidCredentialsException;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.PasswordService;
import com.fundoo.notes.service.TokenService;
import com.fundoo.notes.service.UserRegistrationEventPublisher;
import com.fundoo.notes.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final UserRegistrationEventPublisher registrationEventPublisher;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordService passwordService,
            TokenService tokenService,
            UserRegistrationEventPublisher registrationEventPublisher) {

        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
        this.registrationEventPublisher = registrationEventPublisher;
    }

    @Override
    @Transactional
    public UserResponseDto register(UserRegisterRequestDto registrationRequest) {
        String userEmail = registrationRequest.getEmail().trim().toLowerCase();
        logger.info("Initiating user registration workflow for email: {}", userEmail);

        if (userRepository.findByEmail(userEmail).isPresent()) {
            logger.warn("Registration rejected - duplicate email detected: {}", userEmail);
            throw new DuplicateEmailException("Email is already registered");
        }

        String encryptedPassword = passwordService.encodePassword(registrationRequest.getPassword());
        User newUser = new User(
                registrationRequest.getFirstName().trim(),
                userEmail,
                encryptedPassword
        );

        User persistedUser = userRepository.save(newUser);
        logger.info("User registered and persisted with generated ID: {}", persistedUser.getId());

        // Dispatch asynchronous notification event via message broker
        UserRegistrationEvent registrationEvent = new UserRegistrationEvent(
                persistedUser.getId(),
                persistedUser.getFirstName(),
                persistedUser.getEmail()
        );
        registrationEventPublisher.publish(registrationEvent);
        logger.info("Published asynchronous registration event for user ID: {}", persistedUser.getId());

        return new UserResponseDto(
                persistedUser.getId(),
                persistedUser.getFirstName(),
                persistedUser.getEmail()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        String normalizedEmail = loginRequest.getEmail().trim().toLowerCase();
        logger.info("Processing login authentication attempt for: {}", normalizedEmail);

        User targetUser = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    logger.warn("Authentication failed - user not found for email: {}", normalizedEmail);
                    return new InvalidCredentialsException("Invalid email or password");
                });

        boolean isPasswordValid = passwordService.matchesPassword(
                loginRequest.getPassword(),
                targetUser.getPassword()
        );

        if (!isPasswordValid) {
            logger.warn("Authentication failed - invalid password supplied for email: {}", normalizedEmail);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String jwtAuthToken = tokenService.generateToken(targetUser.getEmail());
        logger.info("Authentication successful - issued JWT token for user ID: {}", targetUser.getId());

        return new LoginResponseDto(jwtAuthToken, "Login successful");
    }
}
