package com.bankingsystem.bank.exception;

public class InvalidTransferException extends RuntimeException{
    public InvalidTransferException(String message) {
        super(message);
    }
}
