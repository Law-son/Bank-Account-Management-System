package main.java.bankapp.models.accounts;

import main.java.bankapp.models.customers.Customer;
import main.java.bankapp.utils.InputValidator;

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
        System.out.println("Transaction Failed: Minimum balance of " + InputValidator.formatAmount(minimumBalance) + " must be maintained.");
        return false;
    }

    @Override
    public void displayAccountDetails() {
        System.out.printf(" %s | %-18s | Savings    | %-15s | %s%n",
                getAccountNumber(), getCustomer().getName(), InputValidator.formatAmount(balance), getStatus());
        System.out.println("        |                    | Interest Rate: " + interestRate + "% | Min Balance: " + InputValidator.formatAmount(minimumBalance));
        System.out.println("-------------------------------------------------------------------------------");
    }


    @Override
    public String getAccountType() {
        return "Savings";
    }

    public double getInterestRate() { return interestRate; }
    public double getMinimumBalance() { return minimumBalance; }
}