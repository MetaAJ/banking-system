package com.bankingsystem.bank.service;

import org.springframework.stereotype.Service;

import com.bankingsystem.bank.dto.LoginRequest;
import com.bankingsystem.bank.dto.SignupRequest;
import com.bankingsystem.bank.entity.Customer;
import com.bankingsystem.bank.entity.Role;
import com.bankingsystem.bank.entity.UserAccount;
import com.bankingsystem.bank.exception.CustomerAlreadyExistsException;
import com.bankingsystem.bank.repository.CustomerRepository;
import com.bankingsystem.bank.repository.UserAccountRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service 
public class AuthService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomerRepository customerRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService (
        AuthenticationManager authenticationManager,
        CustomerRepository customerRepository,
        UserAccountRepository userAccountRepository,
        PasswordEncoder passwordEncoder, 
        JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.customerRepository = customerRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    public String login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(
            request.email(),
            request.password()
        );
        
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        return jwtService.generateToken(authentication);
    }

    @Transactional 
    public void signup(SignupRequest request) {
        if (customerRepository.findByEmail(request.email()).isPresent()) {
            throw new CustomerAlreadyExistsException(
                "Customer with email: " + request.email() + " already exists"
            );
        }

        Customer newCustomer = new Customer(
            request.name(),
            request.email(),
            request.phone()
        );

        customerRepository.save(newCustomer);

        String passwordHash = passwordEncoder.encode(request.password());

        UserAccount newUser = new UserAccount(
            request.email(),
            passwordHash,
            Role.CUSTOMER,
            newCustomer
        );

        userAccountRepository.save(newUser);
    }

}
