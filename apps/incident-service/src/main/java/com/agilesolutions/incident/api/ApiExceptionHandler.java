package com.agilesolutions.incident.api;

import com.agilesolutions.incident.application.IncidentNotFoundException;
import com.agilesolutions.incident.domain.InvalidIncidentTransitionException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IncidentNotFoundException.class)
    ResponseEntity<?> notFound(IncidentNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body("NOT_FOUND", e.getMessage()));
    }
    @ExceptionHandler({InvalidIncidentTransitionException.class, IllegalArgumentException.class})
    ResponseEntity<?> badRequest(RuntimeException e) {
        return ResponseEntity.badRequest().body(body("INVALID_REQUEST", e.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(x -> x.getField() + ": " + x.getDefaultMessage()).findFirst().orElse("Validatiefout");
        return ResponseEntity.badRequest().body(body("VALIDATION_ERROR", message));
    }
    private Map<String,Object> body(String code, String message) {
        return Map.of("timestamp", Instant.now(), "code", code, "message", message);
    }
}
