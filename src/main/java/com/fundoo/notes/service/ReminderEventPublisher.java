
package com.fundoo.notes.service;

import com.fundoo.notes.event.ReminderEvent;

/*
 * Class Name  : ReminderEventPublisher
 * Description : Defines reminder event publishing operations.
 * Author      : Dheeraj Buchha
 * Note        : Refactored and maintained for FundooNotes (Original: Utkarsh Jain)
 * Date        : 16/11/2026
 */

public interface ReminderEventPublisher {

    void publish(ReminderEvent event);
}
