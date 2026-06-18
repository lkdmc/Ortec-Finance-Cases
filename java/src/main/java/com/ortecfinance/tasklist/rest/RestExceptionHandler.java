package com.ortecfinance.tasklist.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Turns the controllers' {@link ResponseStatusException}s into a consistent
 * error body that includes the explanatory message, which the default Spring
 * error response hides.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException exception,
                                                              HttpServletRequest request) {
        HttpStatusCode status = exception.getStatusCode();
        ErrorResponse body = new ErrorResponse(
            status.value(),
            reasonPhrase(status),
            exception.getReason(),
            request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    private String reasonPhrase(HttpStatusCode status) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        return resolved == null ? "" : resolved.getReasonPhrase();
    }
}
