
package com.fundoo.notes.exception;

/*
 * Class Name  : ReminderNotFoundException
 * Description : Handles missing reminders and unauthorized reminder access.
 * Author      : Dheeraj Buchha
 * Note        : Refactored and maintained for FundooNotes (Original: Utkarsh Jain)
 * Date        : 26/11/2026
 */

public class ReminderNotFoundException extends RuntimeException {

    public ReminderNotFoundException(String message) {
        super(message);
    }
}
