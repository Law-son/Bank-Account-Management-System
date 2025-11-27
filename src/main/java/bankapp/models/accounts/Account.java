package main.java.bankapp.models.accounts;

import main.java.bankapp.models.customers.Customer;

public abstract class Account {
    private String accountNumber;
    private Customer customer;
    protected double balance;
    private String status;

    protected static int accountCounter = 0;

    public Account(Customer customer, double initialDeposit) {
        this.accountNumber = "ACC" + String.format("%03d", ++accountCounter); // [cite: 8]
        this.customer = customer;
        this.balance = initialDeposit;
        this.status = "ACTIVE";
    }

    public String getAccountNumber() { return accountNumber; }
    public Customer getCustomer() { return customer; }
    public double getBalance() { return balance; }

    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    public abstract boolean withdraw(double amount);
    public abstract void displayAccountDetails();
    public abstract String getAccountType();
}