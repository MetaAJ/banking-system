package com.bankingsystem.bank.service;

import org.springframework.security.core.userdetails.User;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bankingsystem.bank.entity.UserAccount;
import com.bankingsystem.bank.repository.UserAccountRepository;

@Service 
public class CustomUserDetailsService implements UserDetailsService {
    private final UserAccountRepository userAccountRepository;

    public CustomUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override 
    public UserDetails loadUserByUsername(String email) {
        UserAccount existingUser = userAccountRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "No user found with email: " + email
            )
        );

        UserDetails user = User.withUsername(existingUser.getEmail())
                               .password(existingUser.getPasswordHash())
                               .roles(existingUser.getRole().name())
                               .build();
        
        return user;
    }
}
