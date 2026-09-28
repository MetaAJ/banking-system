package com.bankingsystem.bank.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.bankingsystem.bank.dto.UpdateCustomerRequest;
import com.bankingsystem.bank.entity.Customer;
import com.bankingsystem.bank.entity.UserAccount;
import com.bankingsystem.bank.exception.CustomerAccessDeniedException;
import com.bankingsystem.bank.exception.CustomerAlreadyExistsException;
import com.bankingsystem.bank.exception.CustomerNotFoundException;
import com.bankingsystem.bank.repository.CustomerRepository;
import com.bankingsystem.bank.repository.UserAccountRepository;

import jakarta.transaction.Transactional;

import java.util.List;

@Service 
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final UserAccountRepository userAccountRepository;

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


    private void checkEmailUniqueness(String email) {
        if (customerRepository.findByEmail(email).isPresent()) {
            throw new CustomerAlreadyExistsException(
                "Customer with email: " + email + " already exists"
            );
        }
    }


    private void checkEmailUniqueness(String email, Long currentCustomerId) {
        customerRepository.findByEmail(email)
            .ifPresent(existingCustomer -> {
                if (!existingCustomer.getId().equals(currentCustomerId)) {
                    throw new CustomerAlreadyExistsException(
                        "Customer with email: " + email + " already exists"
                    );
                }
            }
        );
    }


    public CustomerService(
        CustomerRepository customerRepository,
        UserAccountRepository userAccountRepository
    ) {
        this.customerRepository = customerRepository;
        this.userAccountRepository = userAccountRepository;
    }


    public Customer createCustomer(Customer customer) {
        checkEmailUniqueness(customer.getEmail());
        return customerRepository.save(customer);
    }


    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }


    public Customer getCustomerById(Long id) {
        UserAccount user = getAuthenticatedUser();
        Long authenticatedCustomerId = user.getCustomer().getId();                        

        if (!authenticatedCustomerId.equals(id)) {
            throw new CustomerAccessDeniedException(
                "You are not authorized to perform this action"
            );
        }

        return user.getCustomer();
    }


    @Transactional 
    public Customer updateCustomerInfo(Long id, Customer newCustomer) {
        UserAccount user = getAuthenticatedUser();
        Customer customer = getCustomerById(id);

        checkEmailUniqueness(newCustomer.getEmail(),id);

        customer.setEmail(newCustomer.getEmail());
        user.setEmail(newCustomer.getEmail());
        customer.setName(newCustomer.getName());
        customer.setPhone(newCustomer.getPhone());

        userAccountRepository.save(user);
        customerRepository.save(customer);

        return customer;
    }


    @Transactional 
    public Customer patchCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = getCustomerById(id);
        if (request.email() != null) {
            UserAccount user = getAuthenticatedUser();
            checkEmailUniqueness(request.email(),id);
            customer.setEmail(request.email());
            user.setEmail(request.email());
            userAccountRepository.save(user);
        }
        if (request.name() != null) {
            customer.setName(request.name());
        }
        if (request.phone() != null) {
            customer.setPhone(request.phone());
        }

        customerRepository.save(customer);

        return customer;
    }

    
    public void deleteCustomerInfo(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException(
                "No customer found with ID: " + id
            );
        }
        customerRepository.deleteById(id);
    }
}
