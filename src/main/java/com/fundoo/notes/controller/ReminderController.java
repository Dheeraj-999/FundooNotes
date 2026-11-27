package com.fundoo.notes.controller;

/*
 * Class Name  : ReminderController
 * Description : Exposes REST APIs for scheduling, viewing, updating, and removing
 *               note reminders with RabbitMQ notification dispatching.
 * Author      : Dheeraj Buchha * Date        : 27/11/2026
 */

import com.fundoo.notes.dto.request.ReminderRequestDto;
import com.fundoo.notes.dto.request.ReminderUpdateRequestDto;
import com.fundoo.notes.dto.response.ReminderResponseDto;
import com.fundoo.notes.service.ReminderService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /**
     * Schedules a future reminder for a note.
     *
     * @param reminderRequest reminder configuration details
     * @param token           JWT authorization token
     * @return HTTP 201 Created with reminder response
     */
    @PostMapping
    public ResponseEntity<ReminderResponseDto> createReminder(
            @Valid @RequestBody ReminderRequestDto reminderRequest,
            @RequestHeader("Authorization") String token) {

        ReminderResponseDto createdReminder = reminderService.createReminder(token, reminderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReminder);
    }

    /**
     * Retrieves all scheduled reminders for the authenticated user.
     *
     * @param token JWT authorization token
     * @return HTTP 200 OK with list of reminders
     */
    @GetMapping
    public ResponseEntity<List<ReminderResponseDto>> getAllReminders(
            @RequestHeader("Authorization") String token) {

        List<ReminderResponseDto> userReminders = reminderService.getAllReminders(token);
        return ResponseEntity.ok(userReminders);
    }

    /**
     * Updates an existing reminder's trigger timestamp.
     *
     * @param reminderId      target reminder ID
     * @param reminderRequest updated trigger timestamp
     * @param token           JWT authorization token
     * @return HTTP 200 OK with updated reminder
     */
    @PutMapping("/{reminderId}")
    public ResponseEntity<ReminderResponseDto> updateReminder(
            @PathVariable Long reminderId,
            @Valid @RequestBody ReminderUpdateRequestDto reminderRequest,
            @RequestHeader("Authorization") String token) {

        ReminderResponseDto updatedReminder = reminderService.updateReminder(reminderId, token, reminderRequest);
        return ResponseEntity.ok(updatedReminder);
    }

    /**
     * Cancels and deletes an active reminder.
     *
     * @param reminderId target reminder ID
     * @param token      JWT authorization token
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{reminderId}")
    public ResponseEntity<Void> deleteReminder(
            @PathVariable Long reminderId,
            @RequestHeader("Authorization") String token) {

        reminderService.deleteReminder(reminderId, token);
        return ResponseEntity.noContent().build();
    }
}
