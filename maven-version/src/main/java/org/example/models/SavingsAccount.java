package org.example.models;

import org.example.models.exceptions.MinimumBalanceException;
import org.example.utils.ValidationUtils;

public class SavingsAccount extends Account {
    private static final double DEFAULT_INTEREST_RATE = 3.5;
    private static final double DEFAULT_MINIMUM_BALANCE = 500;
    
    private double interestRate = DEFAULT_INTEREST_RATE;
    private double minimumBalance = DEFAULT_MINIMUM_BALANCE;

    public SavingsAccount(Customer customer, double initialDeposit) {
        super(customer, initialDeposit);
    }

    @Override
    public boolean withdraw(double amount) throws MinimumBalanceException {
        if (balance - amount >= minimumBalance) {
            balance -= amount;
            return true;
        }
        throw new MinimumBalanceException("Transaction Failed: Minimum balance of " + ValidationUtils.formatAmount(minimumBalance) + " must be maintained.");
    }

    @Override
    public void displayAccountDetails() {
        System.out.printf(" %s | %-18s | Savings    | %-15s | %s%n",
                getAccountNumber(), getCustomer().getName(), ValidationUtils.formatAmount(balance), getStatus());
        System.out.println("        |                    | Interest Rate: " + interestRate + "% | Min Balance: " + ValidationUtils.formatAmount(minimumBalance));
        System.out.println("-------------------------------------------------------------------------------");
    }


    @Override
    public String getAccountType() {
        return "Savings";
    }

    public double getInterestRate() { return interestRate; }
    public double getMinimumBalance() { return minimumBalance; }
    
    public static double getDefaultInterestRate() { return DEFAULT_INTEREST_RATE; }
    public static double getDefaultMinimumBalance() { return DEFAULT_MINIMUM_BALANCE; }
}

