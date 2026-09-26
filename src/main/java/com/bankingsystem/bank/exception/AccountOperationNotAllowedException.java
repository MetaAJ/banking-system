package com.bankingsystem.bank.exception;

public class AccountOperationNotAllowedException extends RuntimeException{
    public AccountOperationNotAllowedException(String message) {
        super(message);
    }
}
