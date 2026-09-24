package com.bankingsystem.bank.dto;

import java.math.BigDecimal;

import com.bankingsystem.bank.entity.AccountStatus;
import com.bankingsystem.bank.entity.AccountType;

public record AccountResponse(
    Long id,
    String accountNumber,
    AccountType accountType,
    BigDecimal balance,
    AccountStatus status,    
    Long customerId
) {}
