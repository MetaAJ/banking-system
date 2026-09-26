package com.bankingsystem.bank.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.bankingsystem.bank.dto.AccountResponse;
import com.bankingsystem.bank.dto.CreateAccountRequest;
import com.bankingsystem.bank.dto.DepositRequest;
import com.bankingsystem.bank.dto.TransferRequest;
import com.bankingsystem.bank.dto.TransferResponse;
import com.bankingsystem.bank.dto.WithdrawRequest;
import com.bankingsystem.bank.entity.Account;
import com.bankingsystem.bank.entity.AccountStatus;
import com.bankingsystem.bank.entity.Customer;
import com.bankingsystem.bank.entity.Transaction;
import com.bankingsystem.bank.entity.TransactionStatus;
import com.bankingsystem.bank.entity.TransactionType;
import com.bankingsystem.bank.exception.AccountNotFoundException;
import com.bankingsystem.bank.exception.AccountOperationNotAllowedException;
import com.bankingsystem.bank.exception.CustomerNotFoundException;
import com.bankingsystem.bank.exception.InsufficientFundsException;
import com.bankingsystem.bank.exception.InvalidTransferException;
import com.bankingsystem.bank.repository.AccountRepository;
import com.bankingsystem.bank.repository.CustomerRepository;
import com.bankingsystem.bank.repository.TransactionRepository;

import jakarta.transaction.Transactional;

@Service 
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;


    private String generateAccountNumber() {
            return "ACC" + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 10)
                    .toUpperCase();
    }


    private String generateTransactionReference() {
            return "TXN" + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 10)
                    .toUpperCase();
    }


    private AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getAccountNumber(),
            account.getAccountType(),
            account.getBalance(),
            account.getStatus(),
            account.getCustomer().getId()
        );
    }


    private void checkAccountIsActive(Account account) {
        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new AccountOperationNotAllowedException("The account is blocked. Please contact your nearest branch.");
        }
        else if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountOperationNotAllowedException("This account has been closed.");
        }
    }


    public AccountService(
        AccountRepository accountRepository, 
        CustomerRepository customerRepository,
        TransactionRepository transactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }


    public AccountResponse createAccount(CreateAccountRequest request) {
        Optional<Customer> customer = customerRepository.findById(request.customerId());
        if (customer.isEmpty()) {
            throw new CustomerNotFoundException(
                "Customer with ID: " + request.customerId() + " not found!"
            );
        }
        
        Account account = new Account();

        Customer existingCustomer = customer.get();
        account.setCustomer(existingCustomer);
        account.setBalance(BigDecimal.ZERO);
        account.setAccountType(request.accountType());
        account.setStatus(AccountStatus.ACTIVE);

        String accountNumber;

        do {
            accountNumber = generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        account.setAccountNumber(accountNumber);
        Account savedAccount = accountRepository.save(account);
        
        return toAccountResponse(savedAccount);
    }


    public List<AccountResponse> getAccountsByCustomerId(Long customerId) {
        List<Account> accounts = accountRepository.findByCustomerId(customerId);

        List<AccountResponse> responses = new ArrayList<>();
        for (Account account : accounts) {
            AccountResponse response = toAccountResponse(account);
            responses.add(response);
        }
        return responses;
    }


    public AccountResponse getAccountById(Long accountId) {
        Optional<Account> fetchedAccount = accountRepository.findById(accountId);
        if (fetchedAccount.isEmpty()) {
            throw new AccountNotFoundException(
                "No account found with ID: " + accountId
            );
        }
        return toAccountResponse(fetchedAccount.get());
    }


    @Transactional 
    public AccountResponse deposit(Long accountId, DepositRequest request) {
        Account existingAccount = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(
                "No account found with ID: " + accountId
            ));

        checkAccountIsActive(existingAccount);
        
        existingAccount.setBalance(
            existingAccount.getBalance().add(request.amount())
        );

        String transactionReference;
        do {
            transactionReference = generateTransactionReference();
        } while (transactionRepository.existsByTransactionReference(transactionReference));

        Transaction transaction = new Transaction(
                                            transactionReference,
                                            TransactionType.DEPOSIT, 
                                            request.amount(), 
                                            null, 
                                            existingAccount, 
                                            TransactionStatus.SUCCESS,
                                            LocalDateTime.now()
                                        );

        transactionRepository.save(transaction);

        return toAccountResponse(existingAccount);
    }


    @Transactional 
    public AccountResponse withdraw(Long accountId, WithdrawRequest request) {
        Account existingAccount = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException(
                "No account found with ID: " + accountId
            ));
        
        checkAccountIsActive(existingAccount);

        BigDecimal balance = existingAccount.getBalance();
        if (balance.compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient Funds");
        }

        existingAccount.setBalance(
            balance.subtract(request.amount())
        );

        String transactionReference;
        do {
            transactionReference = generateTransactionReference();
        } while (transactionRepository.existsByTransactionReference(transactionReference));

        Transaction transaction = new Transaction(
                                            transactionReference,
                                            TransactionType.WITHDRAWAL, 
                                            request.amount(), 
                                            existingAccount, 
                                            null, 
                                            TransactionStatus.SUCCESS,
                                            LocalDateTime.now()
                                        );

        transactionRepository.save(transaction);

        return toAccountResponse(existingAccount);
    }


    @Transactional 
    public TransferResponse transfer(TransferRequest request) {
        Account sourceAccount = accountRepository.findByAccountNumber(request.fromAccountNumber())
            .orElseThrow(() -> new AccountNotFoundException(
                "No source account found with Acc/No: " + request.fromAccountNumber()
            ));

        Account destinationAccount = accountRepository.findByAccountNumber(request.toAccountNumber())
            .orElseThrow(() -> new AccountNotFoundException(
                "No destination account found with Acc/No: " + request.toAccountNumber()
            ));
        
        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new InvalidTransferException("Source and destination accounts must be different");
        }

        checkAccountIsActive(sourceAccount);
        checkAccountIsActive(destinationAccount);
        
        BigDecimal balance = sourceAccount.getBalance();
        if (balance.compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient Funds");
        }

        sourceAccount.setBalance(
            balance.subtract(request.amount())
        );
        destinationAccount.setBalance(
            destinationAccount.getBalance().add(request.amount())
        );

        String transactionReference;
        do {
            transactionReference = generateTransactionReference();
        } while (transactionRepository.existsByTransactionReference(transactionReference));

        Transaction transaction = new Transaction(
                                            transactionReference,
                                            TransactionType.TRANSFER, 
                                            request.amount(), 
                                            sourceAccount, 
                                            destinationAccount, 
                                            TransactionStatus.SUCCESS,
                                            LocalDateTime.now()
                                        );

        transactionRepository.save(transaction);

        return new TransferResponse(
            request.fromAccountNumber(),
            request.toAccountNumber(),
            request.amount(),
            sourceAccount.getBalance()
        );
    }
}
