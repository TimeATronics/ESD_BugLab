package edu.iiitb.esdlab3.exception;

import edu.iiitb.esdlab3.dto.ApiError;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private ResponseEntity<ApiError> error(
            HttpStatus status,
            String message,
            HttpServletRequest req) {

        return ResponseEntity.status(status).body(
                new ApiError(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        req.getRequestURI()
                )
        );
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ApiError> eventNotFound(
            EventNotFoundException ex,
            HttpServletRequest req) {

        return error(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(RegistrationNotFoundException.class)
    public ResponseEntity<ApiError> registrationNotFound(
            RegistrationNotFoundException ex,
            HttpServletRequest req) {

        return error(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(EventFullException.class)
    public ResponseEntity<ApiError> eventFull(
            EventFullException ex,
            HttpServletRequest req) {

        return error(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateRegistrationException.class)
    public ResponseEntity<ApiError> duplicate(
            DuplicateRegistrationException ex,
            HttpServletRequest req) {

        return error(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(RegistrationConflictException.class)
    public ResponseEntity<ApiError> registrationConflict(
            RegistrationConflictException ex,
            HttpServletRequest req) {

        return error(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalid(
            MethodArgumentNotValidException ex,
            HttpServletRequest req) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Request validation failed");

        return error(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiError> malformed(
            Exception ex,
            HttpServletRequest req) {

        return error(HttpStatus.BAD_REQUEST, "Malformed request", req);
    }
}