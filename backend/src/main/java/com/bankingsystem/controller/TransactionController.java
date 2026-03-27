package com.bankingsystem.controller;

import com.bankingsystem.model.Transaction;
import com.bankingsystem.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<Transaction> deposit(@RequestBody Map<String, Object> body) {
        Long accountId = Long.valueOf(body.get("accountId").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String description = (String) body.get("description");
        return ResponseEntity.ok(transactionService.deposit(accountId, amount, description));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Transaction> withdraw(@RequestBody Map<String, Object> body) {
        Long accountId = Long.valueOf(body.get("accountId").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String description = (String) body.get("description");
        return ResponseEntity.ok(transactionService.withdraw(accountId, amount, description));
    }

    @PostMapping("/transfer")
    public ResponseEntity<List<Transaction>> transfer(@RequestBody Map<String, Object> body) {
        String fromAccount = body.get("fromAccountNumber").toString();
        String toAccount = body.get("toAccountNumber").toString();
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String description = (String) body.get("description");
        return ResponseEntity.ok(transactionService.transfer(fromAccount, toAccount, amount, description));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>> getTransactionsByAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccount(accountId));
    }

    @GetMapping("/account/{accountId}/paged")
    public ResponseEntity<Page<Transaction>> getTransactionsByAccountPaged(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(transactionService.getTransactionsByAccountPaged(accountId, pageable));
    }

    @GetMapping("/account/{accountId}/range")
    public ResponseEntity<List<Transaction>> getTransactionsByDateRange(
            @PathVariable Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(transactionService.getTransactionsByDateRange(accountId, startDate, endDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }
}
