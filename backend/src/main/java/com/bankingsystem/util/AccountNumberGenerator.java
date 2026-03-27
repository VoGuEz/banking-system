package com.bankingsystem.util;

import org.springframework.stereotype.Component;
import java.util.Random;

@Component
public class AccountNumberGenerator {

    private final Random random = new Random();

    public String generate() {
        long accountNumber = 1000000000L + (long)(random.nextDouble() * 9000000000L);
        return String.valueOf(accountNumber);
    }

    public String generateLoanNumber() {
        long loanNumber = 100000L + (long)(random.nextDouble() * 900000L);
        return "LN" + loanNumber;
    }

    public String generateTransactionId() {
        long txnId = 100000000L + (long)(random.nextDouble() * 900000000L);
        return "TXN" + txnId;
    }
}
