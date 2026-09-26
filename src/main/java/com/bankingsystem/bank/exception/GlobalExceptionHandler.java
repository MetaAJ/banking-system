package com.bankingsystem.bank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;

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
    
    @ExceptionHandler (MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
        MethodArgumentNotValidException ex) {
            Map<String, String> errors = new HashMap<>();

            for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errors);
        }

    @ExceptionHandler  (AccountNotFoundException.class)
    public ResponseEntity<String> handleAccountNotFound(
        AccountNotFoundException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());
        }

    @ExceptionHandler (ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<String> handleOptimisticLockingFailure(
        ObjectOptimisticLockingFailureException ex) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Account was modified by another request. Please try again.");
        }

    @ExceptionHandler (InsufficientFundsException.class)
    public ResponseEntity<String> handleInsufficientFunds(
        InsufficientFundsException ex) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ex.getMessage());
        }

    @ExceptionHandler (InvalidTransferException.class)
    public ResponseEntity<String> handleInvalidTransfer(
        InvalidTransferException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ex.getMessage());
        }

    //conflict because request can be valid but the operation conflicts with the current state of the resource
    @ExceptionHandler (AccountOperationNotAllowedException.class)
    public ResponseEntity<String> handleAccountOperationNotAllowed(
        AccountOperationNotAllowedException ex) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ex.getMessage());
        }
}
