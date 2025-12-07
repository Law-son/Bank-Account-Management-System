package org.example.models;

import org.example.models.exceptions.InvalidAmountException;
import org.example.models.exceptions.InsufficientFundsException;
import org.example.models.exceptions.MinimumBalanceException;
import org.example.models.exceptions.OverdraftExceededException;
import org.example.models.interfaces.Transactable;

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

    // All subclasses use the same deposit logic unlike withdraw
    // and the other abstract methods, hence I decided to keep
    // this as a concrete method
    public void deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero.");
        }
        this.balance += amount;
    }

    public abstract boolean withdraw(double amount) throws InsufficientFundsException, MinimumBalanceException, OverdraftExceededException;
    public abstract void displayAccountDetails();
    public abstract String getAccountType();

    @Override
    public boolean processTransaction(double amount, String type) {
        try {
            if (type.equalsIgnoreCase("Deposit")) {
                deposit(amount);
                return true;
            } else if (type.equalsIgnoreCase("Withdrawal")) {
                return withdraw(amount);
            }
        } catch (InvalidAmountException | InsufficientFundsException | MinimumBalanceException | OverdraftExceededException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
        return false;
    }
}

