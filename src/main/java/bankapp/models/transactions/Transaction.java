package main.java.bankapp.models.transactions;

import main.java.bankapp.utils.InputValidator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private static int transactionCounter = 0;

    private String transactionId;
    private String accountNumber;
    private String type; // Deposit or Withdrawal
    private double amount;
    private double balanceAfter;
    private String timestamp;

    public Transaction(String accountNumber, String type, double amount, double balanceAfter) {
        this.transactionId = "TXN" + String.format("%03d", ++transactionCounter);
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public void displayTransactionDetails() {
        System.out.printf("[%s] ID: %s | Type: %s | Amt: %s | Balance: %s%n",
                timestamp, transactionId, type, InputValidator.formatAmount(amount), InputValidator.formatAmount(balanceAfter));
        System.out.println("\nTRANSACTION CONFIRMATION     ");
        System.out.println("---------------------------------");
        System.out.printf("Transaction ID: %s%n", transactionId);
        System.out.printf("Account: %s%n", accountNumber);
        System.out.printf("Type: %s%n", type);
        System.out.printf("Amount: %s%n", InputValidator.formatAmount(amount));
        System.out.printf("Previous Balance: %s%n", InputValidator.formatAmount(balanceAfter));
        System.out.printf("New Balance: %s%n", InputValidator.formatAmount(balanceAfter + amount));
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
}
