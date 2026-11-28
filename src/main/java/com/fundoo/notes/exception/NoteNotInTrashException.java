package com.fundoo.notes.exception;

/*
 * Class Name  : NoteNotInTrashException
 * Description : Raised when a note is permanently deleted before being trashed.
 */

public class NoteNotInTrashException extends RuntimeException {

    public NoteNotInTrashException(String message) {
        super(message);
    }
}
