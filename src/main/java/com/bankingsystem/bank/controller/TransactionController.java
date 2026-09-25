package com.bankingsystem.bank.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bankingsystem.bank.dto.TransactionResponse;
import com.bankingsystem.bank.entity.TransactionType;
import com.bankingsystem.bank.service.TransactionService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController 
@RequestMapping ("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByAccountId(
                @PathVariable Long accountId,
                @RequestParam(required = false) TransactionType transactionType,
                Pageable pageable
            ) {
        Page<TransactionResponse> transactions = transactionService.getTransactionsByAccountId(accountId,transactionType,pageable);

        return ResponseEntity.ok(transactions);
    }
    
}
