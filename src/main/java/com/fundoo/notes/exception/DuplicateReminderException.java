
package com.fundoo.notes.exception;

/*
 * Class Name  : DuplicateReminderException
 * Description : Handles reminders that already exist for a note.
 * Author      : Dheeraj Buchha * Date        : 26/11/2026
 */

public class DuplicateReminderException extends RuntimeException {

    public DuplicateReminderException(String message) {
        super(message);
    }
}
