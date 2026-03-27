package com.bankingsystem.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.util.UUID;

@Component
public class AccountNumberGenerator {

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        // 10-digit account number using SecureRandom for unpredictability
        long accountNumber = 1000000000L + (long)(random.nextDouble() * 9000000000L);
        return String.valueOf(accountNumber);
    }

    public String generateLoanNumber() {
        return "LN" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    public String generateTransactionId() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
