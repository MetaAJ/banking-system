package com.bankingsystem.bank.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record TransferRequest(
    String fromAccountNumber,
    String toAccountNumber,
    @NotNull @DecimalMin("0.01")
    BigDecimal amount
) {}
