package com.bankingsystem.bank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bankingsystem.bank.dto.AccountResponse;
import com.bankingsystem.bank.dto.DepositRequest;
import com.bankingsystem.bank.dto.TransferRequest;
import com.bankingsystem.bank.dto.TransferResponse;
import com.bankingsystem.bank.dto.WithdrawRequest;

import com.bankingsystem.bank.entity.Account;
import com.bankingsystem.bank.entity.Customer;
import com.bankingsystem.bank.entity.Role;
import com.bankingsystem.bank.entity.Transaction;
import com.bankingsystem.bank.entity.UserAccount;

import com.bankingsystem.bank.exception.InsufficientFundsException;
import com.bankingsystem.bank.exception.InvalidTransferException;

import com.bankingsystem.bank.repository.AccountRepository;
import com.bankingsystem.bank.repository.CustomerRepository;
import com.bankingsystem.bank.repository.TransactionRepository;
import com.bankingsystem.bank.repository.UserAccountRepository;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AccountService accountService;


    @BeforeEach
    void setUpSecurityContext() {
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication())
            .thenReturn(
                new UsernamePasswordAuthenticationToken(
                    "1",
                    null
                )
            );
    }


    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }


    private UserAccount createAuthenticatedUser(Customer customer) {
        UserAccount user = new UserAccount(
            customer.getEmail(),
            "passwordHash",
            Role.CUSTOMER,
            customer
        );

        return user;
    }


    @Test
    void deposit_shouldIncreaseBalance() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(10L);
        customer.setName("Alice Brookes");
        customer.setEmail("alice.brookes@example.com");
        customer.setPhone("+91878778778");

        UserAccount user = createAuthenticatedUser(customer);

        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("ACC_DEPOSIT");
        account.setCustomer(customer);
        account.setBalance(new BigDecimal("1000.00"));

        when(userAccountRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(accountRepository.findByAccountNumber("ACC_DEPOSIT"))
            .thenReturn(Optional.of(account));

        when(transactionRepository.existsByTransactionReference(anyString()))
            .thenReturn(false);

        // Act
        DepositRequest request =
            new DepositRequest(new BigDecimal("500.00"));

        AccountResponse response =
            accountService.deposit("ACC_DEPOSIT", request);

        // Assert
        assertEquals(
            new BigDecimal("1500.00"),
            response.balance()
        );

        verify(transactionRepository)
            .save(any(Transaction.class));
    }


    @Test
    void withdraw_shouldDecreaseBalance() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(20L);
        customer.setName("Annabelle Stokes");
        customer.setEmail("annabelle.stokes@example.com");
        customer.setPhone("+91834343022");

        UserAccount user = createAuthenticatedUser(customer);

        Account account = new Account();
        account.setId(2L);
        account.setAccountNumber("ACC_WITHDRAW");
        account.setCustomer(customer);
        account.setBalance(new BigDecimal("1000.00"));

        when(userAccountRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(accountRepository.findByAccountNumber("ACC_WITHDRAW"))
            .thenReturn(Optional.of(account));

        when(transactionRepository.existsByTransactionReference(anyString()))
            .thenReturn(false);

        // Act
        WithdrawRequest request =
            new WithdrawRequest(new BigDecimal("300.00"));

        AccountResponse response =
            accountService.withdraw("ACC_WITHDRAW", request);

        // Assert
        assertEquals(
            new BigDecimal("700.00"),
            response.balance()
        );

        verify(transactionRepository)
            .save(any(Transaction.class));
    }


    @Test
    void withdraw_shouldThrowException_whenInsufficientFunds() {
        // Arrange
        Customer customer = new Customer();
        customer.setId(20L);
        customer.setName("Annabelle Stokes");
        customer.setEmail("annabelle.stokes@example.com");
        customer.setPhone("+91834343022");

        UserAccount user = createAuthenticatedUser(customer);

        Account account = new Account();
        account.setId(2L);
        account.setAccountNumber("ACC_WITHDRAW");
        account.setCustomer(customer);
        account.setBalance(new BigDecimal("500.00"));

        when(userAccountRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(accountRepository.findByAccountNumber("ACC_WITHDRAW"))
            .thenReturn(Optional.of(account));

        // Act
        WithdrawRequest request =
            new WithdrawRequest(new BigDecimal("1000.00"));

        // Assert
        assertThrows(
            InsufficientFundsException.class,
            () -> accountService.withdraw(
                "ACC_WITHDRAW",
                request
            )
        );
    }


    @Test
    void transfer_shouldUpdateBalance() {
        // Arrange
        Customer customer1 = new Customer();
        customer1.setId(10L);
        customer1.setName("Alice Brookes");
        customer1.setEmail("alice.brookes@example.com");
        customer1.setPhone("+91878778778");

        Customer customer2 = new Customer();
        customer2.setId(20L);
        customer2.setName("Annabelle Stokes");
        customer2.setEmail("annabelle.stokes@example.com");
        customer2.setPhone("+91834343022");

        UserAccount user = createAuthenticatedUser(customer1);

        Account sourceAccount = new Account();
        sourceAccount.setId(1L);
        sourceAccount.setAccountNumber("ACC_SOURCE");
        sourceAccount.setCustomer(customer1);
        sourceAccount.setBalance(new BigDecimal("1000.00"));

        Account destinationAccount = new Account();
        destinationAccount.setId(2L);
        destinationAccount.setAccountNumber("ACC_DEST");
        destinationAccount.setCustomer(customer2);
        destinationAccount.setBalance(new BigDecimal("500.00"));

        when(userAccountRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(accountRepository.findByAccountNumber("ACC_SOURCE"))
            .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findByAccountNumber("ACC_DEST"))
            .thenReturn(Optional.of(destinationAccount));

        when(transactionRepository.existsByTransactionReference(anyString()))
            .thenReturn(false);

        // Act
        TransferRequest request =
            new TransferRequest(
                "ACC_SOURCE",
                "ACC_DEST",
                new BigDecimal("300.00")
            );

        TransferResponse response =
            accountService.transfer(request);

        // Assert
        assertEquals(
            new BigDecimal("700.00"),
            response.fromAccountBalance()
        );

        assertEquals(
            new BigDecimal("800.00"),
            destinationAccount.getBalance()
        );

        verify(transactionRepository)
            .save(any(Transaction.class));
    }


    @Test
    void transfer_shouldThrowException_whenSameAccount() {
        // Arrange
        Customer customer1 = new Customer();
        customer1.setId(10L);
        customer1.setName("Alice Brookes");
        customer1.setEmail("alice.brookes@example.com");
        customer1.setPhone("+91878778778");

        UserAccount user = createAuthenticatedUser(customer1);

        Account sourceAccount = new Account();
        sourceAccount.setId(1L);
        sourceAccount.setAccountNumber("ACC_SOURCE");
        sourceAccount.setCustomer(customer1);
        sourceAccount.setBalance(new BigDecimal("1000.00"));

        when(userAccountRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(accountRepository.findByAccountNumber("ACC_SOURCE"))
            .thenReturn(Optional.of(sourceAccount));

        // Act
        TransferRequest request =
            new TransferRequest(
                "ACC_SOURCE",
                "ACC_SOURCE",
                new BigDecimal("2000.00")
            );

        // Assert
        assertThrows(
            InvalidTransferException.class,
            () -> accountService.transfer(request)
        );

        verify(transactionRepository, never())
            .save(any(Transaction.class));
    }


    @Test
    void transfer_shouldThrowException_whenInsufficientFunds() {
        // Arrange
        Customer customer1 = new Customer();
        customer1.setId(10L);
        customer1.setName("Alice Brookes");
        customer1.setEmail("alice.brookes@example.com");
        customer1.setPhone("+91878778778");

        Customer customer2 = new Customer();
        customer2.setId(20L);
        customer2.setName("Annabelle Stokes");
        customer2.setEmail("annabelle.stokes@example.com");
        customer2.setPhone("+91834343022");

        UserAccount user = createAuthenticatedUser(customer1);

        Account sourceAccount = new Account();
        sourceAccount.setId(1L);
        sourceAccount.setAccountNumber("ACC_SOURCE");
        sourceAccount.setCustomer(customer1);
        sourceAccount.setBalance(new BigDecimal("1000.00"));

        Account destinationAccount = new Account();
        destinationAccount.setId(2L);
        destinationAccount.setAccountNumber("ACC_DEST");
        destinationAccount.setCustomer(customer2);
        destinationAccount.setBalance(new BigDecimal("500.00"));

        when(userAccountRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(accountRepository.findByAccountNumber("ACC_SOURCE"))
            .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findByAccountNumber("ACC_DEST"))
            .thenReturn(Optional.of(destinationAccount));

        // Act
        TransferRequest request =
            new TransferRequest(
                "ACC_SOURCE",
                "ACC_DEST",
                new BigDecimal("1000.01")
            );

        // Assert
        assertThrows(
            InsufficientFundsException.class,
            () -> accountService.transfer(request)
        );

        verify(transactionRepository, never())
            .save(any(Transaction.class));
    }
}