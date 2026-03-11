package com.thong.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalException {

    // Handle ResponseStatusException (your custom service errors)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ExceptionResponse<String>> handleServiceException(ResponseStatusException ex) {
        ExceptionResponse<String> errorResponse = ExceptionResponse.<String>builder()
                .message(ex.getMessage())
                .status(ex.getStatusCode().value())
                .timestamp(LocalDateTime.now())
                .detail(ex.getReason())
                .build();

        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    // Handle validation errors (DTO @Valid failures)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse<String>> handleValidationException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ExceptionResponse<String> errorResponse = ExceptionResponse.<String>builder()
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .detail(errors)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    // Handle any unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse<String>> handleUnexpectedException(Exception ex) {
        ExceptionResponse<String> errorResponse = ExceptionResponse.<String>builder()
                .message(ex.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .detail(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDenied(
            AccessDeniedException ex) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ExceptionResponse(
                        "Access Denied",
                        403,
                        LocalDateTime.now(),
                        ex.getMessage()
                ));
    }



    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ExceptionResponse> handleDisabled(DisabledException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ExceptionResponse(
                        "Your account has been deactivated. Please contact your administrator.",
                        403,
                        LocalDateTime.now(),
                        ex.getMessage()
                ));
    }
}