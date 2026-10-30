package com.agilesolutions.commandapi.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return problem(HttpStatus.BAD_REQUEST, "Request validation failed", detail);
    }

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    ProblemDetail handleDownstreamNotFound(HttpClientErrorException.NotFound exception) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found",
                "The requested incident or remediation proposal was not found.");
    }

    @ExceptionHandler(HttpClientErrorException.Conflict.class)
    ProblemDetail handleConflict(HttpClientErrorException.Conflict exception) {
        return problem(HttpStatus.CONFLICT, "Command conflict",
                "The command conflicts with the current incident or approval state.");
    }

    @ExceptionHandler(ResourceAccessException.class)
    ProblemDetail handleDownstreamUnavailable(ResourceAccessException exception) {
        return problem(HttpStatus.BAD_GATEWAY, "Downstream service unavailable",
                "A required platform service could not be reached.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail handleStatus(ResponseStatusException exception) {
        return problem(HttpStatus.valueOf(exception.getStatusCode().value()),
                "Request could not be completed", exception.getReason());
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail result = ProblemDetail.forStatusAndDetail(status, detail == null ? title : detail);
        result.setTitle(title);
        result.setType(URI.create("about:blank"));
        return result;
    }
}
