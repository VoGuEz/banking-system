package com.bankingsystem.controller;

import com.bankingsystem.model.Account;
import com.bankingsystem.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Account.AccountType accountType = Account.AccountType.valueOf(body.get("accountType").toString());
        Account account = accountService.createAccount(userId, accountType);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<Account> getAccountByNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Account>> getAccountsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(accountService.getAccountsByUser(userId));
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Account> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Account.AccountStatus status = Account.AccountStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(accountService.updateAccountStatus(id, status));
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<Account> closeAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.closeAccount(id));
    }
}
