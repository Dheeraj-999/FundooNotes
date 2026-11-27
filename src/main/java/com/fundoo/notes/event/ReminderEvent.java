
package com.fundoo.notes.event;

import java.time.LocalDateTime;

/*
 * Class Name  : ReminderEvent
 * Description : Stores reminder event details for RabbitMQ messaging.
 * Author      : Dheeraj Buchha
 * Note        : Refactored and maintained for FundooNotes (Original: Utkarsh Jain)
 * Date        : 16/11/2026
 */

public record ReminderEvent(
        Long userId,
        Long noteId,
        String email,
        String title,
        LocalDateTime reminderTime
) {
}
