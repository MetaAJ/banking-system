package com.bankingsystem.bank.dto;

import java.math.BigDecimal;

public record TransferResponse(
    String fromAccountNumber,
    String toAccountNumber,
    BigDecimal amount,
    BigDecimal fromAccountBalance
) {}
