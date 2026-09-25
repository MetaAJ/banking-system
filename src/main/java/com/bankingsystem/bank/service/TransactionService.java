package com.bankingsystem.bank.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bankingsystem.bank.dto.TransactionResponse;
import com.bankingsystem.bank.entity.Transaction;
import com.bankingsystem.bank.entity.TransactionType;
import com.bankingsystem.bank.repository.TransactionRepository;

@Service 
public class TransactionService {
    private final TransactionRepository transactionRepository;

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
            transaction.getTransactionReference(),
            transaction.getTransactionType(),
            transaction.getAmount(),
            transaction.getFromAccount() == null
                ? null
                : transaction.getFromAccount().getAccountNumber(),
            transaction.getToAccount() == null
                ? null
                : transaction.getToAccount().getAccountNumber(),
            transaction.getTransactionStatus(),
            transaction.getCreatedAt()
        );
    } 

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Page<TransactionResponse> getTransactionsByAccountId(Long accountId, TransactionType transactionType, Pageable pageable) {

        Page<Transaction> transactions;

        if (transactionType == null) {
            transactions = transactionRepository.findByAccountId(accountId, pageable);
        } else {
            transactions = transactionRepository.findByAccountIdAndTransactionType(accountId, transactionType, pageable);
        }

        return transactions.map(this::toTransactionResponse);
    }
}
