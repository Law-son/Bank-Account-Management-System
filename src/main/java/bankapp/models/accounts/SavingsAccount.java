package main.java.bankapp.models.accounts;

import main.java.bankapp.models.customers.Customer;

public class SavingsAccount extends Account {
    private double interestRate = 3.5;
    private double minimumBalance = 500;

    public SavingsAccount(Customer customer, double initialDeposit) {
        super(customer, initialDeposit);
    }

    @Override
    public boolean withdraw(double amount) {
        if (balance - amount >= minimumBalance) {
            balance -= amount;
            return true;
        }
        System.out.println("Transaction Failed: Minimum balance of $" + minimumBalance + " must be maintained.");
        return false;
    }

    @Override
    public void displayAccountDetails() {
        System.out.println("ACC: " + getAccountNumber() + " | Type: Savings | Balance: $" + balance +
                " | Interest: " + interestRate + "%");
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }

    public double calculateInterest() {
        return balance * (interestRate / 100);
    }
}