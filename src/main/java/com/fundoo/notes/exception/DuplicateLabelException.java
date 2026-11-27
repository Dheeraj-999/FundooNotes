
package com.fundoo.notes.exception;

/*
 * Class Name  : DuplicateLabelException
 * Description : Handles duplicate label creation attempts.
 * Author      : Dheeraj Buchha
 * Note        : Refactored and maintained for FundooNotes (Original: Utkarsh Jain)
 * Date        : 25/11/2026
 */

public class DuplicateLabelException extends RuntimeException {

    public DuplicateLabelException(String message) {
        super(message);
    }
}
