package com.bankingsystem.bank.controller;

import com.bankingsystem.bank.dto.UpdateCustomerRequest;
import com.bankingsystem.bank.entity.Customer;
import com.bankingsystem.bank.service.CustomerService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;




@RestController 
@RequestMapping ("/api/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        Customer createdCustomer = customerService.createCustomer(customer);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdCustomer);
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getCustomers() {
        List<Customer> users = customerService.getAllCustomers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    @PutMapping ("/{id}")
    public ResponseEntity<Customer> updateCustomerInfo(@PathVariable Long id, @Valid @RequestBody Customer customer) {
        Customer updatedCustomer = customerService.updateCustomerInfo(id, customer);
        return ResponseEntity.ok(updatedCustomer);
    }

    @PatchMapping ("/{id}")
    public ResponseEntity<Customer> updateCustomerRequest(@PathVariable Long id, @RequestBody UpdateCustomerRequest request) {
        Customer updatedCustomer = customerService.patchCustomer(id, request);
        return ResponseEntity.ok(updatedCustomer);
    }

    @DeleteMapping ("/{id}")
    public ResponseEntity<Void> deleteCustomerInfo(@PathVariable Long id) {
        customerService.deleteCustomerInfo(id);
        return ResponseEntity.noContent().build();
    }
    
}
