package com.example.bankingapplication.repository;

import com.example.bankingapplication.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;


public interface AccountRepository extends JpaRepository<Account,Long> {

    Optional<Account> findByUsername(String username);
}
