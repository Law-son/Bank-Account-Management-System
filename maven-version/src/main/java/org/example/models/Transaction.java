package org.example.models;

import org.example.utils.ValidationUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a financial transaction in the banking system.
 * Supports date-based sorting and functional programming operations.
 */
public class Transaction {
    private static int transactionCounter = 0;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

    private String transactionId;
    private String accountNumber;
    private String type; // Deposit, Withdrawal, Transfer In, Transfer Out
    private double amount;
    private double balanceAfter;
    private String timestamp;
    private LocalDateTime dateTime; // Stored for efficient sorting

    public Transaction(String accountNumber, String type, double amount, double balanceAfter) {
        this.transactionId = "TXN" + String.format("%03d", ++transactionCounter);
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.dateTime = LocalDateTime.now();
        this.timestamp = dateTime.format(FORMATTER);
    }

    public void displayTransactionDetails() {
        System.out.printf("[%s] ID: %s | Type: %s | Amt: %s | Balance: %s%n",
                timestamp, transactionId, type, ValidationUtils.formatAmount(amount), ValidationUtils.formatAmount(balanceAfter));
        System.out.println("\nTRANSACTION CONFIRMATION     ");
        System.out.println("---------------------------------");
        System.out.printf("Transaction ID: %s%n", transactionId);
        System.out.printf("Account: %s%n", accountNumber);
        System.out.printf("Type: %s%n", type);
        System.out.printf("Amount: %s%n", ValidationUtils.formatAmount(amount));
        double previousBalance = type.equals("Deposit") ? balanceAfter - amount : balanceAfter + amount;
        System.out.printf("Previous Balance: %s%n", ValidationUtils.formatAmount(previousBalance));
        System.out.printf("New Balance: %s%n", ValidationUtils.formatAmount(balanceAfter));
        System.out.printf("Date/Time: %s%n", timestamp);
        System.out.println("---------------------------------");
    }

    // Getters
    public String getAccountNumber() { return accountNumber; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getTransactionID() { return transactionId; }
    public String getDateTime() { return timestamp; }
    public double getBalance() { return balanceAfter; }
    
    /**
     * Gets the LocalDateTime representation for sorting purposes.
     *
     * @return the transaction date/time as LocalDateTime
     */
    public LocalDateTime getDateTimeAsLocalDateTime() {
        return dateTime;
    }
}

