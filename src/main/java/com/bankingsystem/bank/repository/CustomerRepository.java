package com.bankingsystem.bank.repository;

import com.bankingsystem.bank.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
}
