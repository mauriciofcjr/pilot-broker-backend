package com.pilotbroker.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorMessage {

    private final String path;
    private final String method;
    private final int status;
    private final String statusText;
    private final String message;
    private final LocalDateTime timestamp;
    private Map<String, String> errors;

    public ErrorMessage(HttpServletRequest request, HttpStatus status, String message) {
        this.path = request.getRequestURI();
        this.method = request.getMethod();
        this.status = status.value();
        this.statusText = status.getReasonPhrase();
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorMessage(HttpServletRequest request, HttpStatus status,
                        String message, BindingResult result) {
        this(request, status, message);
        this.errors = new HashMap<>();
        for (FieldError fe : result.getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
    }
}
