package com.bankingsystem.bank.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bankingsystem.bank.entity.Transaction;
import com.bankingsystem.bank.entity.TransactionType;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> { 
    boolean existsByTransactionReference(String transactionReference);
    
    @Query("""
        SELECT t
        FROM Transaction t
        WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId)
        ORDER BY t.createdAt DESC
    """)
    Page<Transaction> findByAccountId(
        @Param ("accountId") Long accountId,
        Pageable pageable
    );

    @Query("""
        SELECT t
        FROM Transaction t
        WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId)
        AND t.transactionType = :transactionType
        ORDER BY t.createdAt DESC
    """)
    Page<Transaction> findByAccountIdAndTransactionType(
        @Param ("accountId") Long accountId,
        @Param ("transactionType") TransactionType transactionType,
        Pageable pageable
    );
}
