package org.example.models.exceptions;

public class OverdraftExceededException extends Exception {
    public OverdraftExceededException(String message) {
        super(message);
    }
}

