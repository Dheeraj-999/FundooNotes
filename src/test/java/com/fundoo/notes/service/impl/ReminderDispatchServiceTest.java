package com.fundoo.notes.service.impl;

import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.Reminder;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.EmailDeliveryException;
import com.fundoo.notes.repository.ReminderRepository;
import com.fundoo.notes.service.MailService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderDispatchServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private MailService mailService;

    @InjectMocks
    private ReminderDispatchService service;

    private Reminder dueReminder() {
        User user = new User("Ada", "ada@example.com", "hash");
        Note note = new Note("Launch plan", "Description", user);
        return new Reminder(note, user, LocalDateTime.now().minusMinutes(1));
    }

    @Test
    void emailsADueReminderAndMarksItAsNotified() {
        Reminder reminder = dueReminder();
        when(reminderRepository.findByNotifiedAtIsNullAndReminderTimeLessThanEqual(any(LocalDateTime.class)))
                .thenReturn(List.of(reminder));

        service.dispatchDueReminders();

        verify(mailService).sendReminder("ada@example.com", "Launch plan", reminder.getReminderTime());
        assertNotNull(reminder.getNotifiedAt());
        verify(reminderRepository).save(reminder);
    }

    @Test
    void doesNotMarkAReminderAsNotifiedWhenEmailFails() {
        Reminder reminder = dueReminder();
        when(reminderRepository.findByNotifiedAtIsNullAndReminderTimeLessThanEqual(any(LocalDateTime.class)))
                .thenReturn(List.of(reminder));
        doThrow(new EmailDeliveryException("SMTP down"))
                .when(mailService).sendReminder("ada@example.com", "Launch plan", reminder.getReminderTime());

        service.dispatchDueReminders();

        assertNull(reminder.getNotifiedAt());
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    void doesNothingWhenNoReminderIsDue() {
        when(reminderRepository.findByNotifiedAtIsNullAndReminderTimeLessThanEqual(any(LocalDateTime.class)))
                .thenReturn(List.of());

        service.dispatchDueReminders();

        verifyNoInteractions(mailService);
        verify(reminderRepository, never()).save(any(Reminder.class));
    }
}
