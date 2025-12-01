package main.java.bankapp.models.accounts;

import main.java.bankapp.models.customers.Customer;
import main.java.bankapp.models.transactions.Transactable;

public abstract class Account implements Transactable {
    private String accountNumber;
    private Customer customer;
    protected double balance;
    private String status;

    protected static int accountCounter = 0;

    public Account(Customer customer, double initialDeposit) {
        this.accountNumber = "ACC" + String.format("%03d", ++accountCounter);
        this.customer = customer;
        this.balance = initialDeposit;
        this.status = "ACTIVE";
    }

    public String getAccountNumber() { return accountNumber; }
    public Customer getCustomer() { return customer; }
    public double getBalance() { return balance; }
    public String getStatus() { 
        // Format status for display: "ACTIVE" -> "Active"
        if (status != null && !status.isEmpty()) {
            return status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
        }
        return status;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    public abstract boolean withdraw(double amount);
    public abstract void displayAccountDetails();
    public abstract String getAccountType();

    @Override
    public boolean processTransaction(double amount, String type) {
        if (type.equalsIgnoreCase("Deposit")) {
            deposit(amount);
            return true;
        } else if (type.equalsIgnoreCase("Withdrawal")) {
            return withdraw(amount);
        }
        return false;
    }
}