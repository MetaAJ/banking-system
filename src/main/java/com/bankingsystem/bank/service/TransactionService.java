package com.bankingsystem.bank.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.bankingsystem.bank.dto.TransactionResponse;
import com.bankingsystem.bank.entity.Account;
import com.bankingsystem.bank.entity.Transaction;
import com.bankingsystem.bank.entity.TransactionType;
import com.bankingsystem.bank.entity.UserAccount;
import com.bankingsystem.bank.exception.AccountNotFoundException;
import com.bankingsystem.bank.exception.CustomerAccessDeniedException;
import com.bankingsystem.bank.exception.CustomerNotFoundException;
import com.bankingsystem.bank.repository.AccountRepository;
import com.bankingsystem.bank.repository.TransactionRepository;
import com.bankingsystem.bank.repository.UserAccountRepository;

@Service 
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserAccountRepository userAccountRepository;
    private final AccountRepository accountRepository;

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


    private UserAccount getAuthenticatedUser() {
        String userId = SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getName();

        Long authenticatedUserId = Long.valueOf(userId);

        return userAccountRepository
            .findById(authenticatedUserId)
            .orElseThrow(() -> new CustomerNotFoundException(
                "No user found with ID: " + authenticatedUserId
            ));
    }


    public TransactionService(
        TransactionRepository transactionRepository,
        UserAccountRepository userAccountRepository,
        AccountRepository accountRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.userAccountRepository = userAccountRepository;
        this.accountRepository = accountRepository;
    }

    
    public Page<TransactionResponse> getTransactionsByAccountId(Long accountId, TransactionType transactionType, Pageable pageable) {
        UserAccount user = getAuthenticatedUser();

        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(
                "No account found with Account Id: " + accountId
            ));

        if (!account.getCustomer().getId().equals(user.getCustomer().getId())) {
            throw new CustomerAccessDeniedException(
                "You are not authorized to access this account"
            );
        }

        Page<Transaction> transactions;

        if (transactionType == null) {
            transactions = transactionRepository.findByAccountId(accountId, pageable);
        } else {
            transactions = transactionRepository.findByAccountIdAndTransactionType(accountId, transactionType, pageable);
        }

        return transactions.map(this::toTransactionResponse);
    }
}
