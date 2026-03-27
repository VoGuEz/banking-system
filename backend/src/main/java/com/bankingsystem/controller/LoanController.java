package com.bankingsystem.controller;

import com.bankingsystem.model.Loan;
import com.bankingsystem.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping("/apply")
    public ResponseEntity<Loan> applyForLoan(@RequestBody Map<String, Object> body) {
        Long accountId = Long.valueOf(body.get("accountId").toString());
        Loan.LoanType loanType = Loan.LoanType.valueOf(body.get("loanType").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        Integer termMonths = Integer.valueOf(body.get("termMonths").toString());
        String purpose = (String) body.get("purpose");
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(loanService.applyForLoan(accountId, loanType, amount, termMonths, purpose));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Loan> approveLoan(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(loanService.approveLoan(id, body.get("approvedBy")));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Loan> rejectLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.rejectLoan(id));
    }

    @PostMapping("/{id}/repay")
    public ResponseEntity<Loan> repayLoan(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        return ResponseEntity.ok(loanService.repayLoan(id, amount));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Loan>> getLoansByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(loanService.getLoansByAccount(accountId));
    }

    @GetMapping
    public ResponseEntity<List<Loan>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Loan>> getLoansByStatus(@PathVariable Loan.LoanStatus status) {
        return ResponseEntity.ok(loanService.getLoansByStatus(status));
    }
}
