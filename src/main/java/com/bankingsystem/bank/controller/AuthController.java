package com.bankingsystem.bank.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bankingsystem.bank.dto.LoginRequest;
import com.bankingsystem.bank.dto.SignupRequest;
import com.bankingsystem.bank.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
@RequestMapping ("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
    
    @PostMapping("/signup")
    public void signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
    }
}
