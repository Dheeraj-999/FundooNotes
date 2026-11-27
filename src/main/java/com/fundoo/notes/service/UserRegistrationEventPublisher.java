
package com.fundoo.notes.service;

import com.fundoo.notes.event.UserRegistrationEvent;

/*
 * Class Name  : UserRegistrationEventPublisher
 * Description : Defines the user registration event publishing contract.
 * Author      : Dheeraj Buchha
 * Note        : Refactored and maintained for FundooNotes (Original: Utkarsh Jain)
 * Date        : 16/11/2026
 */

public interface UserRegistrationEventPublisher {

    void publish(UserRegistrationEvent event);
}
