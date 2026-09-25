package com.bankingsystem.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bankingsystem.bank.entity.TransactionStatus;
import com.bankingsystem.bank.entity.TransactionType;

public record TransactionResponse(
    String transactionReference,
    TransactionType transactionType,
    BigDecimal amount,
    String fromAccountNumber,
    String toAccountNumber,
    TransactionStatus transactionStatus,
    LocalDateTime createdAt
) {}
