package com.bankingsystem.bank.exception;

public class CustomerAccessDeniedException extends RuntimeException {

    public CustomerAccessDeniedException(String message) {
        super(message);
    }
}