package com.bankingsystem.bank.dto;

public record UpdateCustomerRequest(
    String name,
    String email,
    String phone
) {

}
