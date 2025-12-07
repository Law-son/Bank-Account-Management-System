package org.example.models;

import org.example.models.exceptions.OverdraftExceededException;
import org.example.utils.ValidationUtils;

public class CheckingAccount extends Account {
    private static final double DEFAULT_OVERDRAFT_LIMIT = 1000;
    private static final double DEFAULT_MONTHLY_FEE = 10;

    private double overdraftLimit = DEFAULT_OVERDRAFT_LIMIT;
    private double monthlyFee = DEFAULT_MONTHLY_FEE;

    public CheckingAccount(Customer customer, double initialDeposit) {
        super(customer, initialDeposit);
    }

    @Override
    public boolean withdraw(double amount) throws OverdraftExceededException {
        if (balance - amount >= -overdraftLimit) {
            balance -= amount;
            return true;
        }
        throw new OverdraftExceededException("Transaction Failed: Exceeds overdraft limit of " + ValidationUtils.formatAmount(overdraftLimit));
    }

    @Override
    public void displayAccountDetails() {
        System.out.printf(" %s | %-18s | Checking   | %-15s | %s%n",
                getAccountNumber(), getCustomer().getName(), ValidationUtils.formatAmount(balance), getStatus());
        System.out.println("        |                    | Overdraft Limit: " + ValidationUtils.formatAmount(overdraftLimit) + " | Monthly Fee: " + ValidationUtils.formatAmount(monthlyFee));
        System.out.println("-------------------------------------------------------------------------------");
    }

    @Override
    public String getAccountType() {
        return "Checking";
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }

    public static double getDefaultOverdraftLimit() {
        return DEFAULT_OVERDRAFT_LIMIT;
    }

    public static double getDefaultMonthlyFee() {
        return DEFAULT_MONTHLY_FEE;
    }

}

