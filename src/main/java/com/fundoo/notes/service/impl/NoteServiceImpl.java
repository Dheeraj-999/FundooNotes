package com.fundoo.notes.service.impl;

/*
 * Class Name  : NoteServiceImpl
 * Description : Production service implementation managing user notes, caching
 *               invalidation, transactional updates, and status toggling.
 * Author      : Dheeraj Buchha * Date        : 27/11/2026
 */

import com.fundoo.notes.dto.request.NoteRequestDto;
import com.fundoo.notes.dto.response.LabelResponseDto;
import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.InvalidCredentialsException;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.NoteService;
import com.fundoo.notes.service.TokenService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {

    private static final Logger logger = LoggerFactory.getLogger(NoteServiceImpl.class);

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public NoteServiceImpl(
            NoteRepository noteRepository,
            UserRepository userRepository,
            TokenService tokenService) {

        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    @CacheEvict(value = "notes", allEntries = true)
    public NoteResponseDto createNote(NoteRequestDto noteRequest, String authorizationToken) {
        logger.info("Initiating note creation workflow");
        User authenticatedUser = extractAuthenticatedUser(authorizationToken);

        Note noteEntity = new Note(
                noteRequest.getTitle().trim(),
                noteRequest.getDescription(),
                authenticatedUser
        );

        Note persistedNote = noteRepository.save(noteEntity);
        logger.info("Successfully persisted Note ID: {} for User ID: {}",
                persistedNote.getId(), authenticatedUser.getId());

        return convertToResponseDto(persistedNote);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "notes", key = "#root.target.getUserIdFromToken(#authorizationToken)")
    public List<NoteResponseDto> getAllNotes(String authorizationToken) {
        logger.info("Fetching notes for authenticated user");
        User authenticatedUser = extractAuthenticatedUser(authorizationToken);

        List<Note> userNotes = noteRepository.findByUserId(authenticatedUser.getId());

        return userNotes.stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "notes", allEntries = true)
    public NoteResponseDto togglePin(Long noteId, String authorizationToken) {
        logger.info("Toggling pin status for Note ID: {}", noteId);
        Note targetNote = findAndValidateNoteOwnership(noteId, authorizationToken);

        targetNote.setPinned(!targetNote.isPinned());
        Note updatedNote = noteRepository.save(targetNote);
        logger.info("Note ID: {} pin status changed to: {}", noteId, updatedNote.isPinned());

        return convertToResponseDto(updatedNote);
    }

    @Override
    @Transactional
    @CacheEvict(value = "notes", allEntries = true)
    public NoteResponseDto toggleArchive(Long noteId, String authorizationToken) {
        logger.info("Toggling archive status for Note ID: {}", noteId);
        Note targetNote = findAndValidateNoteOwnership(noteId, authorizationToken);

        targetNote.setArchived(!targetNote.isArchived());
        if (targetNote.isArchived()) {
            targetNote.setPinned(false);
        }

        Note updatedNote = noteRepository.save(targetNote);
        logger.info("Note ID: {} archive status changed to: {}", noteId, updatedNote.isArchived());

        return convertToResponseDto(updatedNote);
    }

    @Override
    @Transactional
    @CacheEvict(value = "notes", allEntries = true)
    public NoteResponseDto toggleTrash(Long noteId, String authorizationToken) {
        logger.info("Toggling trash status for Note ID: {}", noteId);
        Note targetNote = findAndValidateNoteOwnership(noteId, authorizationToken);

        targetNote.setTrashed(!targetNote.isTrashed());
        if (targetNote.isTrashed()) {
            targetNote.setPinned(false);
        }

        Note updatedNote = noteRepository.save(targetNote);
        logger.info("Note ID: {} trash status changed to: {}", noteId, updatedNote.isTrashed());

        return convertToResponseDto(updatedNote);
    }

    public Long getUserIdFromToken(String authorizationToken) {
        return extractAuthenticatedUser(authorizationToken).getId();
    }

    private Note findAndValidateNoteOwnership(Long noteId, String authorizationToken) {
        User authenticatedUser = extractAuthenticatedUser(authorizationToken);

        Note targetNote = noteRepository.findById(noteId)
                .orElseThrow(() -> {
                    logger.warn("Note not found with ID: {}", noteId);
                    return new NoteNotFoundException("Note not found");
                });

        if (!targetNote.getUser().getId().equals(authenticatedUser.getId())) {
            logger.warn("Unauthorized modification attempt: Note ID: {}, Requester User ID: {}",
                    noteId, authenticatedUser.getId());
            throw new NoteNotFoundException("Note not found");
        }

        return targetNote;
    }

    private User extractAuthenticatedUser(String authorizationToken) {
        if (authorizationToken == null || authorizationToken.isBlank()) {
            logger.warn("Authentication failed: Missing Authorization token");
            throw new InvalidCredentialsException("Authorization token is required");
        }

        String rawJwt = authorizationToken.startsWith("Bearer ")
                ? authorizationToken.substring(7).trim()
                : authorizationToken.trim();

        if (!tokenService.isTokenValid(rawJwt)) {
            logger.warn("Authentication failed: Invalid or expired JWT token");
            throw new InvalidCredentialsException("Invalid or expired token");
        }

        String userEmail = tokenService.extractSubject(rawJwt);
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new InvalidCredentialsException("User associated with token not found"));
    }

    private NoteResponseDto convertToResponseDto(Note noteEntity) {
        List<LabelResponseDto> associatedLabels = noteEntity.getLabels()
                .stream()
                .map(label -> new LabelResponseDto(label.getId(), label.getName()))
                .toList();

        return new NoteResponseDto(
                noteEntity.getId(),
                noteEntity.getTitle(),
                noteEntity.getDescription(),
                noteEntity.isPinned(),
                noteEntity.isArchived(),
                noteEntity.isTrashed(),
                associatedLabels
        );
    }
}
