package com.pilotbroker.exception;

public class UsernameUniqueViolationException extends RuntimeException {
    public UsernameUniqueViolationException(String username) {
        super(String.format("Username '%s' já está cadastrado no sistema.", username));
    }
}
