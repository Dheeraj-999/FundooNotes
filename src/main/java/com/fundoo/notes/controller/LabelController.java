package com.fundoo.notes.controller;

/*
 * Class Name  : LabelController
 * Description : Exposes REST APIs for creating, updating, listing, and assigning
 *               user labels to personal notes.
 * Author      : Dheeraj Buchha
 * Note        : Refactored and maintained for FundooNotes (Original: Utkarsh Jain)
 * Date        : 27/11/2026
 */

import com.fundoo.notes.dto.request.LabelRequestDto;
import com.fundoo.notes.dto.response.LabelResponseDto;
import com.fundoo.notes.service.LabelService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labels")
public class LabelController {

    private final LabelService labelService;

    public LabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    /**
     * Creates a unique label for the authenticated user.
     *
     * @param labelRequest validated label name
     * @param token        JWT authorization token
     * @return HTTP 201 Created with label details
     */
    @PostMapping
    public ResponseEntity<LabelResponseDto> createLabel(
            @Valid @RequestBody LabelRequestDto labelRequest,
            @RequestHeader("Authorization") String token) {

        LabelResponseDto createdLabel = labelService.createLabel(token, labelRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLabel);
    }

    /**
     * Renames an existing label belonging to the authenticated user.
     *
     * @param labelId      target label ID
     * @param labelRequest new label name
     * @param token        JWT authorization token
     * @return HTTP 200 OK with updated label
     */
    @PutMapping("/{labelId}")
    public ResponseEntity<LabelResponseDto> updateLabel(
            @PathVariable Long labelId,
            @Valid @RequestBody LabelRequestDto labelRequest,
            @RequestHeader("Authorization") String token) {

        LabelResponseDto updatedLabel = labelService.updateLabel(labelId, token, labelRequest);
        return ResponseEntity.ok(updatedLabel);
    }

    /**
     * Retrieves all labels created by the authenticated user.
     *
     * @param token JWT authorization token
     * @return HTTP 200 OK with list of user labels
     */
    @GetMapping
    public ResponseEntity<List<LabelResponseDto>> getAllLabels(
            @RequestHeader("Authorization") String token) {

        List<LabelResponseDto> userLabels = labelService.getAllLabels(token);
        return ResponseEntity.ok(userLabels);
    }

    /**
     * Deletes a label owned by the authenticated user.
     *
     * @param labelId target label ID
     * @param token   JWT authorization token
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{labelId}")
    public ResponseEntity<Void> deleteLabel(
            @PathVariable Long labelId,
            @RequestHeader("Authorization") String token) {

        labelService.deleteLabel(labelId, token);
        return ResponseEntity.noContent().build();
    }

    /**
     * Attaches a label to a specific note.
     *
     * @param labelId label ID
     * @param noteId  note ID
     * @param token   JWT authorization token
     * @return HTTP 204 No Content
     */
    @PostMapping("/{labelId}/notes/{noteId}")
    public ResponseEntity<Void> assignLabelToNote(
            @PathVariable Long labelId,
            @PathVariable Long noteId,
            @RequestHeader("Authorization") String token) {

        labelService.assignLabelToNote(labelId, noteId, token);
        return ResponseEntity.noContent().build();
    }

    /**
     * Detaches a label from a specific note.
     *
     * @param labelId label ID
     * @param noteId  note ID
     * @param token   JWT authorization token
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{labelId}/notes/{noteId}")
    public ResponseEntity<Void> removeLabelFromNote(
            @PathVariable Long labelId,
            @PathVariable Long noteId,
            @RequestHeader("Authorization") String token) {

        labelService.removeLabelFromNote(labelId, noteId, token);
        return ResponseEntity.noContent().build();
    }
}
