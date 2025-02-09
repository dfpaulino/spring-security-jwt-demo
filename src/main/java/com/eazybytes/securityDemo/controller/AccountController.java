package com.eazybytes.securityDemo.controller;

import com.eazybytes.securityDemo.model.Accounts;
import com.eazybytes.securityDemo.repository.AccountsRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {

    private final AccountsRepository accountsRepository;

    public AccountController(AccountsRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }

    @GetMapping("/myAccount")
    public  Accounts getAccountDetails (@RequestParam long id) {
        Accounts accounts = accountsRepository.findByCustomerId(id);
        if (accounts != null){
            return  accounts;
        } else {
            return null;
        }
    }

}
