package org.example.models;

import org.example.models.exceptions.InsufficientFundsException;
import org.example.utils.ValidationUtils;

/**
 * Represents a savings account with interest rate and minimum balance requirements.
 */
public class SavingsAccount extends Account {
    private static final double DEFAULT_INTEREST_RATE = 3.5;
    private static final double DEFAULT_MINIMUM_BALANCE = 500;
    
    private double interestRate = DEFAULT_INTEREST_RATE;
    private double minimumBalance = DEFAULT_MINIMUM_BALANCE;

    /**
     * Constructs a new savings account.
     *
     * @param customer       the customer who owns the account
     * @param initialDeposit the initial deposit amount (must meet minimum balance requirement)
     */
    public SavingsAccount(Customer customer, double initialDeposit) {
        super(customer, initialDeposit);
    }

    /**
     * Withdraws the specified amount from the savings account.
     *
     * @param amount the amount to withdraw
     * @return true if withdrawal is successful
     * @throws InsufficientFundsException if withdrawal would violate minimum balance requirement
     */
    @Override
    public boolean withdraw(double amount) throws InsufficientFundsException {
        if (balance - amount >= minimumBalance) {
            balance -= amount;
            return true;
        }
        throw new InsufficientFundsException("Transaction Failed: Insufficient funds. Current balance: " + ValidationUtils.formatAmount(balance));
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

