package com.bankingsystem.service;

import com.bankingsystem.exception.InsufficientFundsException;
import com.bankingsystem.exception.InvalidOperationException;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.model.Account;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.repository.TransactionRepository;
import com.bankingsystem.util.AccountNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountNumberGenerator accountNumberGenerator;

    public Transaction deposit(Long accountId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Deposit amount must be positive");
        }
        
        Account account = accountService.getAccountById(accountId);
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new InvalidOperationException("Account is not active");
        }
        
        account.setBalance(account.getBalance().add(amount));
        accountService.save(account);
        
        Transaction transaction = new Transaction();
        transaction.setTransactionId(accountNumberGenerator.generateTransactionId());
        transaction.setType(Transaction.TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setDescription(description != null ? description : "Deposit");
        transaction.setAccount(account);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        
        return transactionRepository.save(transaction);
    }

    public Transaction withdraw(Long accountId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Withdrawal amount must be positive");
        }
        
        Account account = accountService.getAccountById(accountId);
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new InvalidOperationException("Account is not active");
        }
        
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds. Available balance: " + account.getBalance());
        }
        
        account.setBalance(account.getBalance().subtract(amount));
        accountService.save(account);
        
        Transaction transaction = new Transaction();
        transaction.setTransactionId(accountNumberGenerator.generateTransactionId());
        transaction.setType(Transaction.TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setDescription(description != null ? description : "Withdrawal");
        transaction.setAccount(account);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        
        return transactionRepository.save(transaction);
    }

    public List<Transaction> transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Transfer amount must be positive");
        }
        
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new InvalidOperationException("Cannot transfer to the same account");
        }
        
        Account fromAccount = accountService.getAccountByNumber(fromAccountNumber);
        Account toAccount = accountService.getAccountByNumber(toAccountNumber);
        
        if (fromAccount.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new InvalidOperationException("Source account is not active");
        }
        if (toAccount.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new InvalidOperationException("Destination account is not active");
        }
        
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }
        
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountService.save(fromAccount);
        accountService.save(toAccount);
        
        String txnDesc = description != null ? description : "Fund transfer";
        
        Transaction outTransaction = new Transaction();
        outTransaction.setTransactionId(accountNumberGenerator.generateTransactionId());
        outTransaction.setType(Transaction.TransactionType.TRANSFER_OUT);
        outTransaction.setAmount(amount);
        outTransaction.setBalanceAfter(fromAccount.getBalance());
        outTransaction.setDescription(txnDesc + " to " + toAccountNumber);
        outTransaction.setAccount(fromAccount);
        outTransaction.setReferenceAccountNumber(toAccountNumber);
        outTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        
        Transaction inTransaction = new Transaction();
        inTransaction.setTransactionId(accountNumberGenerator.generateTransactionId());
        inTransaction.setType(Transaction.TransactionType.TRANSFER_IN);
        inTransaction.setAmount(amount);
        inTransaction.setBalanceAfter(toAccount.getBalance());
        inTransaction.setDescription(txnDesc + " from " + fromAccountNumber);
        inTransaction.setAccount(toAccount);
        inTransaction.setReferenceAccountNumber(fromAccountNumber);
        inTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        
        transactionRepository.save(outTransaction);
        transactionRepository.save(inTransaction);
        
        return List.of(outTransaction, inTransaction);
    }

    public List<Transaction> getTransactionsByAccount(Long accountId) {
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    public Page<Transaction> getTransactionsByAccountPaged(Long accountId, Pageable pageable) {
        Account account = accountService.getAccountById(accountId);
        return transactionRepository.findByAccountOrderByCreatedAtDesc(account, pageable);
    }

    public List<Transaction> getTransactionsByDateRange(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByAccountIdAndDateRange(accountId, startDate, endDate);
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
    }
}
