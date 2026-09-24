package com.bankingsystem.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bankingsystem.bank.entity.Account;
import java.util.*;

public interface AccountRepository extends JpaRepository<Account, Long>{
    boolean existsByAccountNumber(String accountNumber);
    List<Account> findByCustomerId(Long customerId);
}
