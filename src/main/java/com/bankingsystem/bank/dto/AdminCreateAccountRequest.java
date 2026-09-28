package com.bankingsystem.bank.dto;

import com.bankingsystem.bank.entity.AccountType;

import jakarta.validation.constraints.NotNull;

public record AdminCreateAccountRequest(
    @NotNull  Long customerId,
    @NotNull AccountType accountType
) {}
