package com.fundoo.notes.controller;

/*
 * Class Name  : NoteController
 * Description : Exposes REST endpoints for creating, retrieving, organizing,
 *               pinning, archiving, and trashing user notes.
 * Author      : Dheeraj Buchha * Date        : 27/11/2026
 */

import com.fundoo.notes.dto.request.NoteRequestDto;
import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.service.NoteService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * Creates a new note entity for the authenticated user.
     *
     * @param noteRequest validated note details
     * @param token       JWT authorization token
     * @return HTTP 201 Created with persisted note details
     */
    @PostMapping
    public ResponseEntity<NoteResponseDto> createNote(
            @Valid @RequestBody NoteRequestDto noteRequest,
            @RequestHeader("Authorization") String token) {

        NoteResponseDto createdNote = noteService.createNote(noteRequest, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);
    }

    /**
     * Retrieves all active notes belonging to the authenticated user.
     *
     * @param token JWT authorization token
     * @return HTTP 200 OK with list of user notes
     */
    @GetMapping
    public ResponseEntity<List<NoteResponseDto>> getAllNotes(
            @RequestHeader("Authorization") String token) {

        List<NoteResponseDto> userNotes = noteService.getAllNotes(token);
        return ResponseEntity.ok(userNotes);
    }

    /**
     * Toggles the pinned status of a note.
     *
     * @param noteId target note primary key
     * @param token  JWT authorization token
     * @return HTTP 200 OK with updated note
     */
    @PatchMapping("/{noteId}/pin")
    public ResponseEntity<NoteResponseDto> togglePin(
            @PathVariable Long noteId,
            @RequestHeader("Authorization") String token) {

        NoteResponseDto updatedNote = noteService.togglePin(noteId, token);
        return ResponseEntity.ok(updatedNote);
    }

    /**
     * Toggles the archived status of a note.
     *
     * @param noteId target note primary key
     * @param token  JWT authorization token
     * @return HTTP 200 OK with updated note
     */
    @PatchMapping("/{noteId}/archive")
    public ResponseEntity<NoteResponseDto> toggleArchive(
            @PathVariable Long noteId,
            @RequestHeader("Authorization") String token) {

        NoteResponseDto updatedNote = noteService.toggleArchive(noteId, token);
        return ResponseEntity.ok(updatedNote);
    }

    /**
     * Toggles the trashed status of a note.
     *
     * @param noteId target note primary key
     * @param token  JWT authorization token
     * @return HTTP 200 OK with updated note
     */
    @PatchMapping("/{noteId}/trash")
    public ResponseEntity<NoteResponseDto> toggleTrash(
            @PathVariable Long noteId,
            @RequestHeader("Authorization") String token) {

        NoteResponseDto updatedNote = noteService.toggleTrash(noteId, token);
        return ResponseEntity.ok(updatedNote);
    }
}
