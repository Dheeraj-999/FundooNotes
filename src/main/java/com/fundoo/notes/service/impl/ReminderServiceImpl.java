package com.fundoo.notes.service.impl;

/*
 * Class Name  : ReminderServiceImpl
 * Description : Production service managing note reminders, asynchronous event
 *               dispatching via RabbitMQ, ownership validation, and scheduling.
 * Author      : Dheeraj Buchha
 * Note        : Refactored and maintained for FundooNotes (Original: Utkarsh Jain)
 * Date        : 27/11/2026
 */

import com.fundoo.notes.dto.request.ReminderRequestDto;
import com.fundoo.notes.dto.request.ReminderUpdateRequestDto;
import com.fundoo.notes.dto.response.ReminderResponseDto;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.Reminder;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.event.ReminderEvent;
import com.fundoo.notes.exception.DuplicateReminderException;
import com.fundoo.notes.exception.InvalidCredentialsException;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.exception.ReminderNotFoundException;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.ReminderRepository;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.ReminderEventPublisher;
import com.fundoo.notes.service.ReminderService;
import com.fundoo.notes.service.TokenService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReminderServiceImpl implements ReminderService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderServiceImpl.class);

    private final ReminderRepository reminderRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final ReminderEventPublisher reminderEventPublisher;

    public ReminderServiceImpl(
            ReminderRepository reminderRepository,
            NoteRepository noteRepository,
            UserRepository userRepository,
            TokenService tokenService,
            ReminderEventPublisher reminderEventPublisher) {

        this.reminderRepository = reminderRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.reminderEventPublisher = reminderEventPublisher;
    }

    @Override
    @Transactional
    public ReminderResponseDto createReminder(String authorizationToken, ReminderRequestDto reminderRequest) {
        User authenticatedUser = extractAuthenticatedUser(authorizationToken);

        Note targetNote = noteRepository.findByIdAndUserId(
                reminderRequest.getNoteId(),
                authenticatedUser.getId()
        ).orElseThrow(() -> new NoteNotFoundException("Note not found"));

        if (reminderRepository.findByNoteIdAndUserId(targetNote.getId(), authenticatedUser.getId()).isPresent()) {
            throw new DuplicateReminderException("Reminder already exists for this note");
        }

        Reminder newReminder = new Reminder(
                targetNote,
                authenticatedUser,
                reminderRequest.getReminderTime()
        );

        Reminder persistedReminder = reminderRepository.save(newReminder);
        publishReminderEvent(persistedReminder, authenticatedUser);

        logger.info("Reminder created successfully. Reminder ID: {}, User ID: {}",
                persistedReminder.getId(), authenticatedUser.getId());

        return convertToResponseDto(persistedReminder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReminderResponseDto> getAllReminders(String authorizationToken) {
        User authenticatedUser = extractAuthenticatedUser(authorizationToken);

        return reminderRepository.findByUserIdOrderByReminderTimeAsc(authenticatedUser.getId())
                .stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public ReminderResponseDto updateReminder(Long reminderId, String authorizationToken, ReminderUpdateRequestDto reminderRequest) {
        User authenticatedUser = extractAuthenticatedUser(authorizationToken);

        Reminder targetReminder = findAndValidateReminderOwnership(reminderId, authenticatedUser);
        targetReminder.setReminderTime(reminderRequest.getReminderTime());

        Reminder updatedReminder = reminderRepository.save(targetReminder);
        publishReminderEvent(updatedReminder, authenticatedUser);

        logger.info("Reminder updated successfully. Reminder ID: {}, User ID: {}", reminderId, authenticatedUser.getId());
        return convertToResponseDto(updatedReminder);
    }

    @Override
    @Transactional
    public void deleteReminder(Long reminderId, String authorizationToken) {
        User authenticatedUser = extractAuthenticatedUser(authorizationToken);
        Reminder targetReminder = findAndValidateReminderOwnership(reminderId, authenticatedUser);

        reminderRepository.delete(targetReminder);
        logger.info("Reminder deleted successfully. Reminder ID: {}, User ID: {}", reminderId, authenticatedUser.getId());
    }

    private Reminder findAndValidateReminderOwnership(Long reminderId, User authenticatedUser) {
        return reminderRepository.findByIdAndUserId(reminderId, authenticatedUser.getId())
                .orElseThrow(() -> new ReminderNotFoundException("Reminder not found"));
    }

    private void publishReminderEvent(Reminder reminder, User authenticatedUser) {
        ReminderEvent reminderEvent = new ReminderEvent(
                authenticatedUser.getId(),
                reminder.getNote().getId(),
                authenticatedUser.getEmail(),
                reminder.getNote().getTitle(),
                reminder.getReminderTime()
        );
        reminderEventPublisher.publish(reminderEvent);
        logger.info("Published reminder dispatch event to RabbitMQ for Note ID: {}", reminder.getNote().getId());
    }

    private User extractAuthenticatedUser(String authorizationToken) {
        if (authorizationToken == null || authorizationToken.isBlank()) {
            throw new InvalidCredentialsException("Authorization token is required");
        }

        String rawJwt = authorizationToken.startsWith("Bearer ")
                ? authorizationToken.substring(7).trim()
                : authorizationToken.trim();

        if (!tokenService.isTokenValid(rawJwt)) {
            throw new InvalidCredentialsException("Invalid or expired token");
        }

        String userEmail = tokenService.extractSubject(rawJwt);
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new InvalidCredentialsException("User associated with token not found"));
    }

    private ReminderResponseDto convertToResponseDto(Reminder reminderEntity) {
        return new ReminderResponseDto(
                reminderEntity.getId(),
                reminderEntity.getNote().getId(),
                reminderEntity.getNote().getTitle(),
                reminderEntity.getReminderTime()
        );
    }
}
