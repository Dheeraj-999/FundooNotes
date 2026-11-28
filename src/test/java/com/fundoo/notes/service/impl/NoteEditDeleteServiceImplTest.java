package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.request.NoteRequestDto;
import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.NoteNotInTrashException;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.ReminderRepository;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.TokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteEditDeleteServiceImplTest {

    private static final String TOKEN = "sample.jwt.token";

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private ReminderRepository reminderRepository;

    @InjectMocks
    private NoteServiceImpl noteService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Ada", "ada@example.com", "hash");
        user.setId(1L);
        when(tokenService.isTokenValid(TOKEN)).thenReturn(true);
        when(tokenService.extractSubject(TOKEN)).thenReturn("ada@example.com");
        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.of(user));
    }

    private Note note(boolean trashed, Long ownerId) {
        User owner = new User("Ada", "ada@example.com", "hash");
        owner.setId(ownerId);
        Note note = new Note("Old title", "Old body", owner);
        note.setId(5L);
        note.setTrashed(trashed);
        return note;
    }

    @Test
    void updatesTitleAndDescriptionOfAnOwnedNote() {
        Note target = note(false, 1L);
        when(noteRepository.findById(5L)).thenReturn(Optional.of(target));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NoteResponseDto response = noteService.updateNote(
                5L, new NoteRequestDto("  New title  ", "New body"), TOKEN);

        assertEquals("New title", response.getTitle());
        assertEquals("New body", response.getDescription());
        verify(noteRepository).save(target);
    }

    @Test
    void permanentlyDeletesATrashedNoteAndItsReminders() {
        Note target = note(true, 1L);
        when(noteRepository.findById(5L)).thenReturn(Optional.of(target));

        noteService.deleteNotePermanently(5L, TOKEN);

        verify(reminderRepository).deleteByNoteId(5L);
        verify(noteRepository).delete(target);
    }

    @Test
    void refusesToPermanentlyDeleteAnActiveNote() {
        Note target = note(false, 1L);
        when(noteRepository.findById(5L)).thenReturn(Optional.of(target));

        assertThrows(NoteNotInTrashException.class,
                () -> noteService.deleteNotePermanently(5L, TOKEN));

        verify(reminderRepository, never()).deleteByNoteId(anyLong());
        verify(noteRepository, never()).delete(any(Note.class));
    }

    @Test
    void refusesToEditAnotherUsersNote() {
        Note target = note(false, 2L);
        when(noteRepository.findById(5L)).thenReturn(Optional.of(target));

        assertThrows(NoteNotFoundException.class,
                () -> noteService.updateNote(5L, new NoteRequestDto("New", "Body"), TOKEN));

        verify(noteRepository, never()).save(any(Note.class));
    }
}
