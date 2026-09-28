package com.bankingsystem.bank.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bankingsystem.bank.dto.AccountResponse;
import com.bankingsystem.bank.dto.AdminCreateAccountRequest;
import com.bankingsystem.bank.service.AccountService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {
    private final AccountService accountService;

    public AdminAccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> adminCreateAccount(@Valid @RequestBody AdminCreateAccountRequest request) {
        AccountResponse createdAccount = accountService.adminCreateAccount(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdAccount);
    }
}