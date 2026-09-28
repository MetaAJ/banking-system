package com.bankingsystem.bank.entity;

import jakarta.persistence.*;

@Entity 
@Table (name = "users")
public class UserAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (unique = true, nullable = false)
    private String email;

    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne 
    @JoinColumn (name = "customer_id", nullable = false)
    private Customer customer;

    protected UserAccount() {
    }

    public UserAccount(
        String email,
        String passwordHash,
        Role role,
        Customer customer
    ) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.customer = customer;
    }

    public Long getId() {
    return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public Customer getCustomer() {
        return customer;
    }
}
