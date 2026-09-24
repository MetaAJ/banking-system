package com.bankingsystem.bank.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController 
public class BankController {
    
    @GetMapping("/status")
    public String status() {
        return "Banking API is running!";
    }
}
