package com.fundoo.notes.controller;

import com.fundoo.notes.dto.request.NoteRequestDto;
import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.service.NoteService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteEditDeleteControllerTest {

    @Mock
    private NoteService noteService;

    @InjectMocks
    private NoteController noteController;

    @Test
    void updateReturnsOkWithTheUpdatedNote() {
        NoteRequestDto request = new NoteRequestDto("New title", "New body");
        NoteResponseDto response = mock(NoteResponseDto.class);
        when(noteService.updateNote(5L, request, "Bearer token")).thenReturn(response);

        ResponseEntity<NoteResponseDto> result =
                noteController.updateNote(5L, request, "Bearer token");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(noteService).updateNote(5L, request, "Bearer token");
    }

    @Test
    void permanentDeleteReturnsNoContent() {
        ResponseEntity<Void> result = noteController.deleteNote(5L, "Bearer token");

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(noteService).deleteNotePermanently(5L, "Bearer token");
    }
}
