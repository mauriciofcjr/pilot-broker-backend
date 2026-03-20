package com.pilotbroker.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorMessage {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String method;
    private Map<String, String> errors;

    public ErrorMessage(HttpServletRequest request, HttpStatus status, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        this.path = request.getRequestURI();
        this.method = request.getMethod();
    }

    public ErrorMessage(HttpServletRequest request, HttpStatus status, String message, BindingResult result) {
        this(request, status, message);
        addErrors(result);
    }

    private void addErrors(BindingResult result) {
        this.errors = new HashMap<>();
        for (FieldError fieldError : result.getFieldErrors()) {
            this.errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public String getMethod() { return method; }
    public Map<String, String> getErrors() { return errors; }
}
