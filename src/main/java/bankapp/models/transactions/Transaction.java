package main.java.bankapp.models.transactions;

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
        System.out.printf("[%s] ID: %s | Type: %s | Amt: $%.2f | Balance: $%.2f%n",
                timestamp, transactionId, type, amount, balanceAfter);
    }

    // Getters
    public String getAccountNumber() { return accountNumber; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
}
