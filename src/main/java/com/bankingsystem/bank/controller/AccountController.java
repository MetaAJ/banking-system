package com.bankingsystem.bank.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bankingsystem.bank.dto.AccountResponse;
import com.bankingsystem.bank.dto.CreateAccountRequest;
import com.bankingsystem.bank.dto.DepositRequest;
import com.bankingsystem.bank.dto.TransferRequest;
import com.bankingsystem.bank.dto.WithdrawRequest;
import com.bankingsystem.bank.service.AccountService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@RestController 
@RequestMapping ("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse createdAccount = accountService.createAccount(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdAccount);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAccountsByCustomerId(@RequestParam Long customerId) {
        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(customerId);

        return ResponseEntity.ok(accounts);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        AccountResponse account = accountService.getAccountById(id);

        return ResponseEntity.ok(account);
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<AccountResponse> deposit(@PathVariable Long id, @Valid @RequestBody DepositRequest request) {
        AccountResponse updatedAccount = accountService.deposit(id, request);

        return ResponseEntity.ok(updatedAccount);
    }
    
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<AccountResponse> withdraw(@PathVariable Long id, @Valid @RequestBody WithdrawRequest request) {
        AccountResponse updatedAccount = accountService.withdraw(id, request);

        return ResponseEntity.ok(updatedAccount);
    }
    
    @PostMapping("/transfer")
    public ResponseEntity<AccountResponse> transfer(@Valid @RequestBody TransferRequest request) {
        AccountResponse updatedAccount = accountService.transfer(request);

        return ResponseEntity.ok(updatedAccount);
    }
    
    
}
