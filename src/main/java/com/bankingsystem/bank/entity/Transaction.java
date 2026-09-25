package com.bankingsystem.bank.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;

@Entity 
@Table (name = "transactions")
public class Transaction {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (unique = true, nullable = false)
    private String transactionReference;

    @Enumerated (EnumType.STRING)
    private TransactionType transactionType;

    @DecimalMin (value = "0.01")
    private BigDecimal amount;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "from_account_id")
    private Account fromAccount;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "to_account_id")
    private Account toAccount;

    @Enumerated (EnumType.STRING)
    private TransactionStatus transactionStatus;

    private LocalDateTime createdAt;

    // Required by JPA/Hibernate
    protected Transaction() {
    }

    // Used by our application when creating a transaction
    public Transaction(
        String transactionReference,
        TransactionType transactionType,
        BigDecimal amount,
        Account fromAccount,
        Account toAccount,
        TransactionStatus transactionStatus,
        LocalDateTime createdAt
    ) {
        this.transactionReference = transactionReference;
        this.transactionType = transactionType;
        this.amount = amount;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.transactionStatus = transactionStatus;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

}

