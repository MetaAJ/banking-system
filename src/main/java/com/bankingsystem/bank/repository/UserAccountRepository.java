package com.bankingsystem.bank.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bankingsystem.bank.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount ,Long> {
    Optional<UserAccount> findByEmail(String email);
}
