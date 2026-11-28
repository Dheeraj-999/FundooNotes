package com.fundoo.notes.exception;

/*
 * Class Name  : GlobalExceptionHandler
 * Description : Centralized @RestControllerAdvice handling exceptions application-wide,
 *               transforming domain and validation exceptions into standardized RFC-compliant
 *               error responses.
 * Author      : Dheeraj Buchha * Date        : 27/11/2026
 */

import com.fundoo.notes.dto.response.ApiErrorResponseDto;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiErrorResponseDto> handleDuplicateEmail(
            DuplicateEmailException exception,
            HttpServletRequest request) {

        return constructErrorResponse(HttpStatus.CONFLICT, exception.getMessage(), request, null);
    }

    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDto> handleNoteNotFound(
            NoteNotFoundException exception,
            HttpServletRequest request) {

        return constructErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, null);
    }

    @ExceptionHandler(NoteNotInTrashException.class)
    public ResponseEntity<ApiErrorResponseDto> handleNoteNotInTrash(
            NoteNotInTrashException exception,
            HttpServletRequest request) {

        return constructErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request, null);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request) {

        return constructErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDto> handleValidationErrors(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> fieldValidationErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldValidationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return constructErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, fieldValidationErrors);
    }

    @ExceptionHandler(DuplicateLabelException.class)
    public ResponseEntity<ApiErrorResponseDto> handleDuplicateLabel(
            DuplicateLabelException exception,
            HttpServletRequest request) {

        return constructErrorResponse(HttpStatus.CONFLICT, exception.getMessage(), request, null);
    }

    @ExceptionHandler(LabelNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDto> handleLabelNotFound(
            LabelNotFoundException exception,
            HttpServletRequest request) {

        return constructErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, null);
    }

    @ExceptionHandler(ReminderNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDto> handleReminderNotFound(
            ReminderNotFoundException exception,
            HttpServletRequest request) {

        return constructErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateReminderException.class)
    public ResponseEntity<ApiErrorResponseDto> handleDuplicateReminder(
            DuplicateReminderException exception,
            HttpServletRequest request) {

        return constructErrorResponse(HttpStatus.CONFLICT, exception.getMessage(), request, null);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInvalidOtp(
            InvalidOtpException exception,
            HttpServletRequest request) {

        return constructErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request, null);
    }

    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<ApiErrorResponseDto> handleEmailDeliveryFailure(
            EmailDeliveryException exception,
            HttpServletRequest request) {

        return constructErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDto> handleGenericException(
            Exception exception,
            HttpServletRequest request) {

        logger.error("Unhandled exception intercepted at URI: {}", request.getRequestURI(), exception);
        return constructErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, null);
    }

    private ResponseEntity<ApiErrorResponseDto> constructErrorResponse(
            HttpStatus httpStatus,
            String errorMessage,
            HttpServletRequest request,
            Map<String, String> fieldErrors) {

        ApiErrorResponseDto errorDto = new ApiErrorResponseDto(
                LocalDateTime.now(),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                errorMessage,
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.status(httpStatus).body(errorDto);
    }
}
