package com.fundoo.notes.service.impl;

/*
 * Class Name  : LabelServiceImpl
 * Description : Production service managing user-defined note labels, ensuring
 *               user-level uniqueness, transactional safety, and cache synchronization.
 * Author      : Dheeraj Buchha
 * Note        : Refactored and maintained for FundooNotes (Original: Utkarsh Jain)
 * Date        : 27/11/2026
 */

import com.fundoo.notes.dto.request.LabelRequestDto;
import com.fundoo.notes.dto.response.LabelResponseDto;
import com.fundoo.notes.entity.Label;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.DuplicateLabelException;
import com.fundoo.notes.exception.InvalidCredentialsException;
import com.fundoo.notes.exception.LabelNotFoundException;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.repository.LabelRepository;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.LabelService;
import com.fundoo.notes.service.TokenService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LabelServiceImpl implements LabelService {

    private static final Logger logger = LoggerFactory.getLogger(LabelServiceImpl.class);

    private final LabelRepository labelRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public LabelServiceImpl(
            LabelRepository labelRepository,
            NoteRepository noteRepository,
            UserRepository userRepository,
            TokenService tokenService) {

        this.labelRepository = labelRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    public LabelResponseDto createLabel(String authorizationToken, LabelRequestDto labelRequest) {
        User authenticatedUser = extractAuthenticatedUser(authorizationToken);
        String normalizedLabelName = labelRequest.getName().trim();

        if (labelRepository.findByNameIgnoreCaseAndUserId(normalizedLabelName, authenticatedUser.getId()).isPresent()) {
            throw new DuplicateLabelException("Label already exists");
        }

        Label newLabel = new Label(normalizedLabelName, authenticatedUser);
        Label persistedLabel = labelRepository.save(newLabel);

        logger.info("Label successfully created. Label ID: {}, User ID: {}",
                persistedLabel.getId(), authenticatedUser.getId());

        return convertToResponseDto(persistedLabel);
    }

    @Override
    @Transactional
    @CacheEvict(value = "notes", allEntries = true)
    public LabelResponseDto updateLabel(Long labelId, String authorizationToken, LabelRequestDto labelRequest) {
        User authenticatedUser = extractAuthenticatedUser(authorizationToken);
        String normalizedLabelName = labelRequest.getName().trim();

        Label targetLabel = labelRepository.findById(labelId)
                .orElseThrow(() -> new LabelNotFoundException("Label not found"));

        if (!targetLabel.getUser().getId().equals(authenticatedUser.getId())) {
            throw new LabelNotFoundException("Label not found");
        }

        if (labelRepository.existsByNameIgnoreCaseAndUserIdAndIdNot(
                normalizedLabelName, authenticatedUser.getId(), labelId)) {
            throw new DuplicateLabelException("Label already exists");
        }

        targetLabel.setName(normalizedLabelName);
        Label updatedLabel = labelRepository.save(targetLabel);

        logger.info("Label updated successfully. Label ID: {}, User ID: {}", labelId, authenticatedUser.getId());
        return convertToResponseDto(updatedLabel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabelResponseDto> getAllLabels(String authorizationToken) {
        User authenticatedUser = extractAuthenticatedUser(authorizationToken);

        return labelRepository.findByUserId(authenticatedUser.getId())
                .stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "notes", allEntries = true)
    public void deleteLabel(Long labelId, String authorizationToken) {
        User authenticatedUser = extractAuthenticatedUser(authorizationToken);

        Label targetLabel = labelRepository.findById(labelId)
                .orElseThrow(() -> new LabelNotFoundException("Label not found"));

        if (!targetLabel.getUser().getId().equals(authenticatedUser.getId())) {
            throw new LabelNotFoundException("Label not found");
        }

        List<Note> notesWithLabel = noteRepository.findByUserId(authenticatedUser.getId());
        for (Note note : notesWithLabel) {
            if (note.getLabels().remove(targetLabel)) {
                noteRepository.save(note);
            }
        }

        labelRepository.delete(targetLabel);
        logger.info("Label deleted successfully. Label ID: {}, User ID: {}", labelId, authenticatedUser.getId());
    }

    @Override
    @Transactional
    @CacheEvict(value = "notes", allEntries = true)
    public void assignLabelToNote(Long labelId, Long noteId, String authorizationToken) {
        User authenticatedUser = extractAuthenticatedUser(authorizationToken);

        Label targetLabel = labelRepository.findById(labelId)
                .orElseThrow(() -> new LabelNotFoundException("Label not found"));

        if (!targetLabel.getUser().getId().equals(authenticatedUser.getId())) {
            throw new LabelNotFoundException("Label not found");
        }

        Note targetNote = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException("Note not found"));

        if (!targetNote.getUser().getId().equals(authenticatedUser.getId())) {
            throw new NoteNotFoundException("Note not found");
        }

        targetNote.getLabels().add(targetLabel);
        noteRepository.save(targetNote);
        logger.info("Assigned Label ID: {} to Note ID: {}", labelId, noteId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "notes", allEntries = true)
    public void removeLabelFromNote(Long labelId, Long noteId, String authorizationToken) {
        User authenticatedUser = extractAuthenticatedUser(authorizationToken);

        Label targetLabel = labelRepository.findById(labelId)
                .orElseThrow(() -> new LabelNotFoundException("Label not found"));

        if (!targetLabel.getUser().getId().equals(authenticatedUser.getId())) {
            throw new LabelNotFoundException("Label not found");
        }

        Note targetNote = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException("Note not found"));

        if (!targetNote.getUser().getId().equals(authenticatedUser.getId())) {
            throw new NoteNotFoundException("Note not found");
        }

        targetNote.getLabels().remove(targetLabel);
        noteRepository.save(targetNote);
        logger.info("Removed Label ID: {} from Note ID: {}", labelId, noteId);
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

    private LabelResponseDto convertToResponseDto(Label labelEntity) {
        return new LabelResponseDto(labelEntity.getId(), labelEntity.getName());
    }
}
