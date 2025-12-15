package org.example.models;

import org.example.models.exceptions.InsufficientFundsException;
import org.example.utils.ValidationUtils;

/**
 * Represents a checking account with overdraft limit and monthly fee.
 */
public class CheckingAccount extends Account {
    private static final double DEFAULT_OVERDRAFT_LIMIT = 1000;
    private static final double DEFAULT_MONTHLY_FEE = 10;

    private double overdraftLimit = DEFAULT_OVERDRAFT_LIMIT;
    private double monthlyFee = DEFAULT_MONTHLY_FEE;

    /**
     * Constructs a new checking account.
     *
     * @param customer       the customer who owns the account
     * @param initialDeposit the initial deposit amount
     */
    public CheckingAccount(Customer customer, double initialDeposit) {
        super(customer, initialDeposit);
    }

    /**
     * Checks if a withdrawal of the specified amount is possible.
     * Synchronized for thread-safe balance checks.
     *
     * @param amount the amount to check
     * @return true if withdrawal is within overdraft limit, false otherwise
     */
    @Override
    public synchronized boolean canWithdraw(double amount) {
        return balance - amount >= -overdraftLimit;
    }
    
    /**
     * Withdraws the specified amount from the checking account.
     * Synchronized to prevent race conditions during concurrent withdrawals.
     *
     * @param amount the amount to withdraw
     * @return true if withdrawal is successful
     * @throws InsufficientFundsException if withdrawal would exceed available balance and overdraft limit
     */
    @Override
    public synchronized boolean withdraw(double amount) throws InsufficientFundsException {
        if (balance - amount >= -overdraftLimit) {
            balance -= amount;
            return true;
        }
        throw new InsufficientFundsException("Transaction Failed: Insufficient funds. Current balance: " + ValidationUtils.formatAmount(balance));
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

