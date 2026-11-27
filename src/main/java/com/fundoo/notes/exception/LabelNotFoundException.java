
package com.fundoo.notes.exception;

/*
 * Class Name  : LabelNotFoundException
 * Description : Handles missing labels and unauthorized label access.
 * Author      : Dheeraj Buchha
 * Note        : Refactored and maintained for FundooNotes (Original: Utkarsh Jain)
 * Date         : 25/11/2026
 */

public class LabelNotFoundException extends RuntimeException {

    public LabelNotFoundException(String message) {
        super(message);
    }
}
