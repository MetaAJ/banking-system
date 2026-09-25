package com.bankingsystem.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bankingsystem.bank.entity.Transaction;

import java.util.*;

public interface TransactionRepository extends JpaRepository<Transaction, Long> { 
    boolean existsByTransactionReference(String transactionReference);
    List<Transaction> findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(
        Long fromAccountId,
        Long toAccountId
    );
}
