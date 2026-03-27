package com.bankingsystem.controller;

import com.bankingsystem.model.Account;
import com.bankingsystem.model.Loan;
import com.bankingsystem.model.User;
import com.bankingsystem.service.AccountService;
import com.bankingsystem.service.LoanService;
import com.bankingsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private LoanService loanService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        List<User> users = userService.getAllUsers();
        List<Account> accounts = accountService.getAllAccounts();
        List<Loan> loans = loanService.getAllLoans();
        
        stats.put("totalUsers", users.size());
        stats.put("activeUsers", users.stream().filter(u -> u.getStatus() == User.UserStatus.ACTIVE).count());
        stats.put("totalAccounts", accounts.size());
        stats.put("activeAccounts", accounts.stream().filter(a -> a.getStatus() == Account.AccountStatus.ACTIVE).count());
        stats.put("totalLoans", loans.size());
        stats.put("pendingLoans", loans.stream().filter(l -> l.getStatus() == Loan.LoanStatus.PENDING).count());
        stats.put("activeLoans", loans.stream().filter(l -> l.getStatus() == Loan.LoanStatus.ACTIVE).count());
        stats.put("totalLoanAmount", loans.stream()
            .filter(l -> l.getStatus() == Loan.LoanStatus.ACTIVE)
            .mapToDouble(l -> l.getOutstandingBalance().doubleValue())
            .sum());
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        users.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(users);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/loans/pending")
    public ResponseEntity<List<Loan>> getPendingLoans() {
        return ResponseEntity.ok(loanService.getLoansByStatus(Loan.LoanStatus.PENDING));
    }
}
