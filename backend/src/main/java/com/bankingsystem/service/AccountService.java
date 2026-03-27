package com.bankingsystem.service;

import com.bankingsystem.exception.InvalidOperationException;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.model.Account;
import com.bankingsystem.model.User;
import com.bankingsystem.repository.AccountRepository;
import com.bankingsystem.util.AccountNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountNumberGenerator accountNumberGenerator;

    public Account createAccount(Long userId, Account.AccountType accountType) {
        User user = userService.getUserById(userId);
        
        Account account = new Account();
        String accountNumber;
        do {
            accountNumber = accountNumberGenerator.generate();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        
        account.setAccountNumber(accountNumber);
        account.setAccountType(accountType);
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);
        
        if (accountType == Account.AccountType.SAVINGS) {
            account.setInterestRate(new BigDecimal("3.50"));
        } else if (accountType == Account.AccountType.FIXED_DEPOSIT) {
            account.setInterestRate(new BigDecimal("6.00"));
        }
        
        return accountRepository.save(account);
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
    }

    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
    }

    public List<Account> getAccountsByUser(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account updateAccountStatus(Long id, Account.AccountStatus status) {
        Account account = getAccountById(id);
        if (account.getStatus() == Account.AccountStatus.CLOSED) {
            throw new InvalidOperationException("Cannot update a closed account");
        }
        account.setStatus(status);
        return accountRepository.save(account);
    }

    public Account closeAccount(Long id) {
        Account account = getAccountById(id);
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new InvalidOperationException("Cannot close account with non-zero balance");
        }
        account.setStatus(Account.AccountStatus.CLOSED);
        return accountRepository.save(account);
    }

    public Account save(Account account) {
        return accountRepository.save(account);
    }
}
