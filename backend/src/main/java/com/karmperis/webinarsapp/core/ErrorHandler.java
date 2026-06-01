package com.karmperis.webinarsapp.core;

import com.karmperis.webinarsapp.core.exceptions.EntityAlreadyExistsException;
import com.karmperis.webinarsapp.core.exceptions.EntityInvalidArgumentException;
import com.karmperis.webinarsapp.core.exceptions.EntityNotFoundException;
import com.karmperis.webinarsapp.core.exceptions.ValidationException;
import com.karmperis.webinarsapp.dto.ErrorResponseDTO;
import com.karmperis.webinarsapp.dto.ValidationErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Global REST exception handler that maps known exceptions to HTTP error responses.
 */
@ControllerAdvice
@Slf4j
public class ErrorHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles {@link ValidationException}. HTTP 400.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponseDTO> handleValidationException(ValidationException e) {
        log.warn("Validation Failed. Message={}", e.getMessage());

        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return new ResponseEntity<>(new ValidationErrorResponseDTO(e.getCode(), e.getMessage(), errors),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link EntityNotFoundException}. HTTP 404.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleEntityNotFoundException(EntityNotFoundException e) {
        log.warn("Entity not found. Message={}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO(e.getCode(), e.getMessage()));
    }

    /**
     * Handles {@link EntityInvalidArgumentException}. HTTP 400.
     */
    @ExceptionHandler(EntityInvalidArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidArgumentException(EntityInvalidArgumentException e) {
        log.warn("Invalid Argument. Message={}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO(e.getCode(), e.getMessage()));
    }

    /**
     * Handles {@link EntityAlreadyExistsException}. HTTP 409.
     */
    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleEntityAlreadyExistsException(EntityAlreadyExistsException e) {
        log.warn("Entity already exists. Message={}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDTO(e.getCode(), e.getMessage()));
    }

    /**
     * Handles {@link DataAccessException}. HTTP 500.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleDatabaseException(DataAccessException e) {
        log.error("Database error. Message={}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("DATABASE_ERROR", "A database error occurred."));
    }

    /**
     * Handles any uncaught exception. HTTP 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception e) {
        log.error("Unexpected error. Message={}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO("INTERNAL_SERVER_ERROR", "An unexpected error occurred."));
    }

    /**
     * Handles {@link AuthenticationException}. HTTP 401.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("Failed login for IP={}", request.getRemoteAddr());

        String errorCode = switch (e) {
            case BadCredentialsException ignored -> "INVALID_CREDENTIALS";
            case DisabledException ignored -> "ACCOUNT_DISABLED";
            case LockedException ignored -> "ACCOUNT_LOCKED";
            case AccountExpiredException ignored -> "ACCOUNT_EXPIRED";
            case CredentialsExpiredException ignored -> "CREDENTIALS_EXPIRED";
            default -> "AUTHENTICATION_ERROR";
        };

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO(errorCode, e.getMessage()));
    }

    /**
     * Handles {@link AccessDeniedException}. HTTP 403.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied. Message={}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponseDTO("ACCESS_DENIED", e.getMessage()));
    }
}