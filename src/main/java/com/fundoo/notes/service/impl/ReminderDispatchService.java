package com.fundoo.notes.service.impl;

import com.fundoo.notes.entity.Reminder;
import com.fundoo.notes.exception.EmailDeliveryException;
import com.fundoo.notes.repository.ReminderRepository;
import com.fundoo.notes.service.MailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/*
 * Class Name  : ReminderDispatchService
 * Description : Polls for due reminders and emails each one exactly once.
 *               The frontend shows a browser alert while the tab is open; this
 *               job is the backup that still reaches the user when it is closed.
 */
@Service
public class ReminderDispatchService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderDispatchService.class);

    private final ReminderRepository reminderRepository;
    private final MailService mailService;

    public ReminderDispatchService(ReminderRepository reminderRepository, MailService mailService) {
        this.reminderRepository = reminderRepository;
        this.mailService = mailService;
    }

    @Scheduled(
            initialDelayString = "${app.reminder.initial-delay-ms:10000}",
            fixedDelayString = "${app.reminder.check-interval-ms:30000}")
    @Transactional
    public void dispatchDueReminders() {
        List<Reminder> dueReminders = reminderRepository
                .findByNotifiedAtIsNullAndReminderTimeLessThanEqual(LocalDateTime.now());

        for (Reminder reminder : dueReminders) {
            try {
                mailService.sendReminder(
                        reminder.getUser().getEmail(),
                        reminder.getNote().getTitle(),
                        reminder.getReminderTime());
                reminder.setNotifiedAt(LocalDateTime.now());
                reminderRepository.save(reminder);
                logger.info("Reminder {} emailed to {}", reminder.getId(), reminder.getUser().getEmail());
            } catch (EmailDeliveryException exception) {
                // Leave notifiedAt empty so the next tick retries the delivery.
                logger.error("Failed to email reminder {}", reminder.getId(), exception);
            }
        }
    }
}
