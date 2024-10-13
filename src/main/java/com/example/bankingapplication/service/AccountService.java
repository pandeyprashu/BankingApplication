package com.example.bankingapplication.service;

import com.example.bankingapplication.repository.AccountRepository;
import com.example.bankingapplication.repository.TransactionRepository;
import com.example.bankingapplication.model.Account;
import com.example.bankingapplication.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class AccountService implements UserDetailsService {

    @Autowired
     PasswordEncoder passwordEncoder;

    @Autowired
    AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    public Account findAccountByUserName(String username) {
        return accountRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void registerAccount(String username, String password) {
        if (accountRepository.findByUsername((username)).isPresent()) {
            throw new RuntimeException("Username already exists");

        }
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setBalance(BigDecimal.ZERO);
        accountRepository.save(account);

    }

    public void deposit(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setType("deposit");
        transaction.setTimeStamp(LocalDateTime.now());
        transaction.setAccount(account);
        transactionRepository.save(transaction);
    }

    public void withdraw(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setType("withdraw");
        transaction.setTimeStamp(LocalDateTime.now());
        transaction.setAccount(account);
        transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionHistory(Account account) {
        return transactionRepository.findByAccountId(account.getId());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account=findAccountByUserName(username);
        if(account==null){
            throw new UsernameNotFoundException("User not found");
        }
        Account newAccount = new Account();
        newAccount.setUsername(account.getUsername());
        newAccount.setPassword(account.getPassword());
        newAccount.setBalance(account.getBalance());
        newAccount.setTransactions(account.getTransactions());
        newAccount.setAuthorities(authorities());

        return  newAccount;
    }

    public Collection<? extends GrantedAuthority> authorities(){
        return Arrays.asList(new SimpleGrantedAuthority("User"));
    }

    public void transferAmount(Account fromAccount,String toUsername,BigDecimal amount){
        if(fromAccount.getBalance().compareTo(amount)<0){
            throw new RuntimeException("Insufficient funds");
        }
        Account toAccount=accountRepository.findByUsername(toUsername).orElseThrow(() -> new RuntimeException("User not found"));

        //Deduct from fromAccount
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountRepository.save(fromAccount);

        //Add to toAccount
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(toAccount);

        //Add transaction
        Transaction fromTransaction=new Transaction();
        fromTransaction.setAmount(amount);
        fromTransaction.setType("transfer to"+toUsername);
        fromTransaction.setTimeStamp(LocalDateTime.now());
        fromTransaction.setAccount(fromAccount);
        transactionRepository.save(fromTransaction);

        Transaction toTransaction=new Transaction();
        toTransaction.setAmount(amount);
        toTransaction.setType("transfer");
        toTransaction.setTimeStamp(LocalDateTime.now());
        toTransaction.setAccount(toAccount);
        transactionRepository.save(toTransaction);

    }
}
