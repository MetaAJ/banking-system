package com.bankingsystem.bank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
    @NotBlank 
    @Size (min = 2, max = 50)
    String name,

    @NotBlank
    @Email 
    @Size(max = 50)
    String email,

    @NotBlank
    @Pattern (regexp = "^\\+[1-9]\\d{7,14}$", message = "Phone number must be in international format")
    String phone,

    @NotBlank
    @Size(min = 6, max = 20)
    String password
) {}
