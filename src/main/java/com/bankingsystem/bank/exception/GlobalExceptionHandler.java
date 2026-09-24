package com.bankingsystem.bank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice 
public class GlobalExceptionHandler {
    
    @ExceptionHandler (CustomerAlreadyExistsException.class)
    public ResponseEntity<String> handleCustomerAlreadyExists(
        CustomerAlreadyExistsException ex) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
        }
    
    @ExceptionHandler (CustomerNotFoundException.class)
    public ResponseEntity<String> handleCustomerNotFound(
        CustomerNotFoundException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());
        }
    
}
