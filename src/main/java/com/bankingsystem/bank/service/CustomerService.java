package com.bankingsystem.bank.service;

import org.springframework.stereotype.Service;

import com.bankingsystem.bank.dto.UpdateCustomerRequest;
import com.bankingsystem.bank.entity.Customer;
import com.bankingsystem.bank.exception.CustomerAlreadyExistsException;
import com.bankingsystem.bank.exception.CustomerNotFoundException;
import com.bankingsystem.bank.repository.CustomerRepository;

import java.util.List;
import java.util.Optional;

@Service 
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(Customer customer) {
        if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            throw new CustomerAlreadyExistsException(
                "Customer with email: " + customer.getEmail() + " already exists"
            );
        }
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        Optional<Customer> foundCustomer = customerRepository.findById(id);
        if (foundCustomer.isEmpty()) {
            throw new CustomerNotFoundException(
                "No customer found with ID: " + id
            );
        }
        return foundCustomer.get();
    }

    public Customer updateCustomerInfo(Long id, Customer newCustomer) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            Customer existingCustomer = customer.get();

            existingCustomer.setEmail(newCustomer.getEmail());
            existingCustomer.setName(newCustomer.getName());
            existingCustomer.setPhone(newCustomer.getPhone());

            customerRepository.save(existingCustomer);

            return existingCustomer;
        }
        throw new CustomerNotFoundException(
            "No customer found with ID: " + id
        );
    }

    public Customer patchCustomer(Long id, UpdateCustomerRequest request) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            Customer existingCustomer = customer.get();
            if (request.email() != null) {
                existingCustomer.setEmail(request.email());
            }
            if (request.name() != null) {
                existingCustomer.setName(request.name());
            }
            if (request.phone() != null) {
                existingCustomer.setPhone(request.phone());
            }

            customerRepository.save(existingCustomer);

            return existingCustomer;
        }
        throw new CustomerNotFoundException(
            "No customer found with ID: " + id
        );
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
