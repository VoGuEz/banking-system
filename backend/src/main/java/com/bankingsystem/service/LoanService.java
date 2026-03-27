package com.bankingsystem.service;

import com.bankingsystem.exception.InsufficientFundsException;
import com.bankingsystem.exception.InvalidOperationException;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.model.Account;
import com.bankingsystem.model.Loan;
import com.bankingsystem.model.Transaction;
import com.bankingsystem.repository.LoanRepository;
import com.bankingsystem.repository.TransactionRepository;
import com.bankingsystem.util.AccountNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountNumberGenerator accountNumberGenerator;

    public Loan applyForLoan(Long accountId, Loan.LoanType loanType, BigDecimal amount, Integer termMonths, String purpose) {
        Account account = accountService.getAccountById(accountId);
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new InvalidOperationException("Account is not active");
        }
        
        BigDecimal interestRate = getDefaultInterestRate(loanType);
        BigDecimal monthlyPayment = calculateMonthlyPayment(amount, interestRate, termMonths);
        
        Loan loan = new Loan();
        loan.setLoanNumber(accountNumberGenerator.generateLoanNumber());
        loan.setLoanType(loanType);
        loan.setPrincipalAmount(amount);
        loan.setOutstandingBalance(amount);
        loan.setInterestRate(interestRate);
        loan.setTermMonths(termMonths);
        loan.setMonthlyPayment(monthlyPayment);
        loan.setStatus(Loan.LoanStatus.PENDING);
        loan.setAccount(account);
        loan.setPurpose(purpose);
        
        return loanRepository.save(loan);
    }

    public Loan approveLoan(Long loanId, String approvedBy) {
        Loan loan = getLoanById(loanId);
        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new InvalidOperationException("Loan is not in PENDING status");
        }
        
        loan.setStatus(Loan.LoanStatus.APPROVED);
        loan.setApprovedBy(approvedBy);
        loan.setApprovedDate(LocalDate.now());
        loan.setStartDate(LocalDate.now());
        loan.setEndDate(LocalDate.now().plusMonths(loan.getTermMonths()));
        
        Account account = loan.getAccount();
        account.setBalance(account.getBalance().add(loan.getPrincipalAmount()));
        accountService.save(account);
        
        Transaction disbursement = new Transaction();
        disbursement.setTransactionId(accountNumberGenerator.generateTransactionId());
        disbursement.setType(Transaction.TransactionType.LOAN_DISBURSEMENT);
        disbursement.setAmount(loan.getPrincipalAmount());
        disbursement.setBalanceAfter(account.getBalance());
        disbursement.setDescription("Loan disbursement - " + loan.getLoanNumber());
        disbursement.setAccount(account);
        disbursement.setStatus(Transaction.TransactionStatus.COMPLETED);
        transactionRepository.save(disbursement);
        
        loan.setStatus(Loan.LoanStatus.ACTIVE);
        return loanRepository.save(loan);
    }

    public Loan rejectLoan(Long loanId) {
        Loan loan = getLoanById(loanId);
        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new InvalidOperationException("Loan is not in PENDING status");
        }
        loan.setStatus(Loan.LoanStatus.REJECTED);
        return loanRepository.save(loan);
    }

    public Loan repayLoan(Long loanId, BigDecimal amount) {
        Loan loan = getLoanById(loanId);
        if (loan.getStatus() != Loan.LoanStatus.ACTIVE) {
            throw new InvalidOperationException("Loan is not active");
        }
        
        Account account = loan.getAccount();
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for loan repayment");
        }
        
        BigDecimal repayAmount = amount.min(loan.getOutstandingBalance());
        account.setBalance(account.getBalance().subtract(repayAmount));
        accountService.save(account);
        
        loan.setOutstandingBalance(loan.getOutstandingBalance().subtract(repayAmount));
        
        Transaction repayment = new Transaction();
        repayment.setTransactionId(accountNumberGenerator.generateTransactionId());
        repayment.setType(Transaction.TransactionType.LOAN_REPAYMENT);
        repayment.setAmount(repayAmount);
        repayment.setBalanceAfter(account.getBalance());
        repayment.setDescription("Loan repayment - " + loan.getLoanNumber());
        repayment.setAccount(account);
        repayment.setStatus(Transaction.TransactionStatus.COMPLETED);
        transactionRepository.save(repayment);
        
        if (loan.getOutstandingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(Loan.LoanStatus.PAID_OFF);
            loan.setOutstandingBalance(BigDecimal.ZERO);
        }
        
        return loanRepository.save(loan);
    }

    public Loan getLoanById(Long id) {
        return loanRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", id));
    }

    public List<Loan> getLoansByAccount(Long accountId) {
        return loanRepository.findByAccountId(accountId);
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public List<Loan> getLoansByStatus(Loan.LoanStatus status) {
        return loanRepository.findByStatus(status);
    }

    private BigDecimal getDefaultInterestRate(Loan.LoanType loanType) {
        return switch (loanType) {
            case PERSONAL -> new BigDecimal("12.00");
            case HOME -> new BigDecimal("8.50");
            case AUTO -> new BigDecimal("9.00");
            case EDUCATION -> new BigDecimal("7.00");
            case BUSINESS -> new BigDecimal("11.00");
        };
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal annualRate, int termMonths) {
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRPowN = onePlusR.pow(termMonths);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRPowN);
        BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public void applyMonthlyInterest() {
        List<Account> savingsAccounts = accountService.getAllAccounts()
            .stream()
            .filter(a -> a.getAccountType() == Account.AccountType.SAVINGS && a.getStatus() == Account.AccountStatus.ACTIVE)
            .toList();
        
        for (Account account : savingsAccounts) {
            BigDecimal monthlyRate = account.getInterestRate().divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);
            BigDecimal interest = account.getBalance().multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            
            if (interest.compareTo(BigDecimal.ZERO) > 0) {
                account.setBalance(account.getBalance().add(interest));
                accountService.save(account);
                
                Transaction interestTxn = new Transaction();
                interestTxn.setTransactionId(accountNumberGenerator.generateTransactionId());
                interestTxn.setType(Transaction.TransactionType.INTEREST);
                interestTxn.setAmount(interest);
                interestTxn.setBalanceAfter(account.getBalance());
                interestTxn.setDescription("Monthly interest credit");
                interestTxn.setAccount(account);
                interestTxn.setStatus(Transaction.TransactionStatus.COMPLETED);
                transactionRepository.save(interestTxn);
            }
        }
    }
}
