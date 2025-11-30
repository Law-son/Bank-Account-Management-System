package main.java.bankapp.models.accounts;

import main.java.bankapp.models.customers.Customer;
import main.java.bankapp.utils.InputValidator;

public class CheckingAccount extends Account {
    private double overdraftLimit = 1000;
    private double monthlyFee = 10;

    public CheckingAccount(Customer customer, double initialDeposit) {
        super(customer, initialDeposit);
    }

    @Override
    public boolean withdraw(double amount) {
        if (balance - amount >= -overdraftLimit) {
            balance -= amount;
            return true;
        }
        System.out.println("Transaction Failed: Exceeds overdraft limit of " + InputValidator.formatAmount(overdraftLimit));
        return false;
    }

    @Override
    public void displayAccountDetails() {
        System.out.printf(" %s | %-18s | Checking   | %-15s | %s%n",
                getAccountNumber(), getCustomer().getName(), InputValidator.formatAmount(balance), getStatus());
        System.out.println("        |                    | Overdraft Limit: " + InputValidator.formatAmount(overdraftLimit) + " | Monthly Fee: " + InputValidator.formatAmount(monthlyFee));
        System.out.println("-------------------------------------------------------------------------------");
    }

    @Override
    public String getAccountType() {
        return "Checking";
    }

    public double getOverdraftLimit() { return overdraftLimit; }
    public double getMonthlyFee() { return monthlyFee; }

}