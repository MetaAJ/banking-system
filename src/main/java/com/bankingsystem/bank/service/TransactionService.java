package com.bankingsystem.bank.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bankingsystem.bank.dto.TransactionResponse;
import com.bankingsystem.bank.entity.Transaction;
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

    public List<TransactionResponse> getTransactionsByAccountId(Long accountId) {
        List<Transaction> transactions = transactionRepository.findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc
        (
            accountId, accountId
        );

        List<TransactionResponse> responses = new ArrayList<>();

        for (Transaction transaction : transactions) {
            responses.add(toTransactionResponse(transaction));
        }

        return responses;
        
    }
}
