package com.bankingsystem.bank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "customers")
public class Customer {
    
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank 
    @Size (min = 2, max = 50)
    private String name;

    @NotBlank 
    @Email 
    @Size (max = 50)
    @Column (unique = true)
    private String email;

    @NotBlank 
    @Pattern (regexp = "^\\+[1-9]\\d{7,14}$",
              message = "Phone number must be in international format"
    )
    private String phone;

    //Constructor
    public Customer() {
    }

    //Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }
}
