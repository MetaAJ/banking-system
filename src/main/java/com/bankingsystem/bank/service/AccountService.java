package com.bankingsystem.bank.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.bankingsystem.bank.dto.AccountResponse;
import com.bankingsystem.bank.dto.AdminCreateAccountRequest;
import com.bankingsystem.bank.dto.CreateAccountRequest;
import com.bankingsystem.bank.dto.DepositRequest;
import com.bankingsystem.bank.dto.TransferRequest;
import com.bankingsystem.bank.dto.TransferResponse;
import com.bankingsystem.bank.dto.WithdrawRequest;
import com.bankingsystem.bank.entity.Account;
import com.bankingsystem.bank.entity.AccountStatus;
import com.bankingsystem.bank.entity.AccountType;
import com.bankingsystem.bank.entity.Customer;
import com.bankingsystem.bank.entity.Transaction;
import com.bankingsystem.bank.entity.TransactionStatus;
import com.bankingsystem.bank.entity.TransactionType;
import com.bankingsystem.bank.entity.UserAccount;
import com.bankingsystem.bank.exception.AccountNotFoundException;
import com.bankingsystem.bank.exception.AccountOperationNotAllowedException;
import com.bankingsystem.bank.exception.CustomerAccessDeniedException;
import com.bankingsystem.bank.exception.CustomerNotFoundException;
import com.bankingsystem.bank.exception.InsufficientFundsException;
import com.bankingsystem.bank.exception.InvalidTransferException;
import com.bankingsystem.bank.repository.AccountRepository;
import com.bankingsystem.bank.repository.CustomerRepository;
import com.bankingsystem.bank.repository.TransactionRepository;
import com.bankingsystem.bank.repository.UserAccountRepository;

import jakarta.transaction.Transactional;

@Service 
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final UserAccountRepository userAccountRepository;


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


    private AccountResponse createAccountForCustomer(Customer customer, AccountType accountType) {
        Account account = new Account();

        account.setCustomer(customer);
        account.setBalance(BigDecimal.ZERO);
        account.setAccountType(accountType);
        account.setStatus(AccountStatus.ACTIVE);

        String accountNumber;

        do {
            accountNumber = generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        account.setAccountNumber(accountNumber);
        Account savedAccount = accountRepository.save(account);
        
        return toAccountResponse(savedAccount);
    }


    private void checkAccountIsActive(Account account) {
        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new AccountOperationNotAllowedException("The account is blocked. Please contact your nearest branch.");
        }
        else if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountOperationNotAllowedException("This account has been closed.");
        }
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


    private Account verifyAccount(String accountNumber) {
        UserAccount user = getAuthenticatedUser();
        Long authenticatedCustomerId = user.getCustomer().getId();                 

        Account account = accountRepository
            .findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException(
                "No account found with Acc/No: " + accountNumber
            ));

        if(!account.getCustomer().getId().equals(authenticatedCustomerId)) {
            throw new CustomerAccessDeniedException(
                "You are not authorized to access this account"
            );
        }

        return account;
    }


    public AccountService(
        AccountRepository accountRepository, 
        CustomerRepository customerRepository,
        TransactionRepository transactionRepository,
        UserAccountRepository userAccountRepository
    ) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.userAccountRepository = userAccountRepository;
    }


    public AccountResponse adminCreateAccount(AdminCreateAccountRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
            .orElseThrow(() -> new CustomerNotFoundException(
                "Customer with ID: " + request.customerId() + " not found!"
            ));
        
        return createAccountForCustomer(customer, request.accountType());
        
    }


    public AccountResponse createAccount(CreateAccountRequest request) {
        UserAccount user = getAuthenticatedUser();
        return createAccountForCustomer(user.getCustomer(), request.accountType());
    }


    public List<AccountResponse> getAccountsByCustomerId(Long customerId) {
        UserAccount user = getAuthenticatedUser();
        Long authenticatedCustomerId = user.getCustomer().getId();

        if (!authenticatedCustomerId.equals(customerId)) {
            throw new CustomerAccessDeniedException(
                "You are not authorized to perform this action"
            );
        }

        List<Account> accounts = accountRepository.findByCustomerId(authenticatedCustomerId);

        List<AccountResponse> responses = new ArrayList<>();
        for (Account account : accounts) {
            AccountResponse response = toAccountResponse(account);
            responses.add(response);
        }
        return responses;
    }


    public AccountResponse getAccountByAccountNumber(String accountNumber) {
        Account account = verifyAccount(accountNumber);
        return toAccountResponse(account);
    }


    @Transactional 
    public AccountResponse deposit(String accountNumber, DepositRequest request) {
        Account account = verifyAccount(accountNumber);
        checkAccountIsActive(account);
        
        account.setBalance(
            account.getBalance().add(request.amount())
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
                                            account, 
                                            TransactionStatus.SUCCESS,
                                            LocalDateTime.now()
                                        );

        transactionRepository.save(transaction);

        return toAccountResponse(account);
    }


    @Transactional 
    public AccountResponse withdraw(String accountNumber, WithdrawRequest request) {
        Account account = verifyAccount(accountNumber);
        checkAccountIsActive(account);

        BigDecimal balance = account.getBalance();
        if (balance.compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient Funds");
        }

        account.setBalance(
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
                                            account, 
                                            null, 
                                            TransactionStatus.SUCCESS,
                                            LocalDateTime.now()
                                        );

        transactionRepository.save(transaction);

        return toAccountResponse(account);
    }


    @Transactional 
    public TransferResponse transfer(TransferRequest request) {
        Account sourceAccount = verifyAccount(request.fromAccountNumber());

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
