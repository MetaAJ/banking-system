package com.bankingsystem.bank.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record WithdrawRequest(
    @NotNull 
    @DecimalMin (value = "0.01")
    BigDecimal amount
) {}
