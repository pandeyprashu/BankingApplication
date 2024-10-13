package com.example.bankingapplication.controller;


import com.example.bankingapplication.model.Account;
import com.example.bankingapplication.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class BankController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        String username= SecurityContextHolder.getContext().getAuthentication().getName();
        Account account=accountService.findAccountByUserName(username);
        model.addAttribute("account",account);
        model.addAttribute("balance",account.getBalance());
        return "dashboard";
    }

    @GetMapping("/register")
    public  String showRegistrationForm(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String registerAccount(@RequestParam String username, @RequestParam String password, Model model) {
        try{
            accountService.registerAccount(username, password);
            return "redirect:/login";

        }catch(RuntimeException e){
            model.addAttribute("Error",e.getMessage());
            return  "register";
        }
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam BigDecimal amount){
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        Account account=accountService.findAccountByUserName(username);
        accountService.deposit(account,amount);
        return "redirect:/dashboard";

    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam BigDecimal amount,Model model) {
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        Account account=accountService.findAccountByUserName(username);
       try{
           accountService.withdraw(account,amount);
       }catch (RuntimeException e){
           model.addAttribute("error",e.getMessage());
           model.addAttribute("account",account);
       }
        return "redirect:/dashboard";

    }

    @GetMapping("/transactions")
    public String transactionHistory(Model model) {
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        Account account=accountService.findAccountByUserName(username);
        model.addAttribute("transactions",accountService.getTransactionHistory(account));
        return "transactions";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam BigDecimal amount, @RequestParam String username,Model model) {
        String usernameFrom=SecurityContextHolder.getContext().getAuthentication().getName();
        Account fromAccount=accountService.findAccountByUserName(usernameFrom);
        try{
            accountService.transferAmount(fromAccount,username,amount);
        }catch (RuntimeException e){
                model.addAttribute("error",e.getMessage());
                model.addAttribute("account",fromAccount);
                return "dashboard";
        }
        return "redirect:/dashboard";
    }
}
