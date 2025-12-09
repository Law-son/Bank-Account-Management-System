package org.example.factories;

import org.example.models.Account;
import org.example.models.CheckingAccount;
import org.example.models.Customer;
import org.example.models.SavingsAccount;

/**
 * Factory class for creating Account instances.
 * Follows Factory Pattern to support Open/Closed Principle.
 * New account types can be added without modifying existing code.
 */
public class AccountFactory {
    
    /**
     * Creates an account based on the specified type.
     *
     * @param accountType     the type of account (1 = Savings, 2 = Checking)
     * @param customer        the customer to associate with the account
     * @param initialDeposit  the initial deposit amount
     * @return the created account instance
     * @throws IllegalArgumentException if accountType is invalid
     */
    public static Account createAccount(int accountType, Customer customer, double initialDeposit) {
        return switch (accountType) {
            case 1 -> new SavingsAccount(customer, initialDeposit);
            case 2 -> new CheckingAccount(customer, initialDeposit);
            default -> throw new IllegalArgumentException("Invalid account type: " + accountType);
        };
    }
    
    /**
     * Gets the available account types for display.
     *
     * @return array of account type descriptions
     */
    public static String[] getAccountTypeDescriptions() {
        return new String[]{
            String.format("Savings Account (Interest: %.1f%%, Min Balance: %s)",
                    SavingsAccount.getDefaultInterestRate(),
                    formatAmount(SavingsAccount.getDefaultMinimumBalance())),
            String.format("Checking Account (Overdraft: %s, Monthly Fee: %s)",
                    formatAmount(CheckingAccount.getDefaultOverdraftLimit()),
                    formatAmount(CheckingAccount.getDefaultMonthlyFee()))
        };
    }
    
    /**
     * Formats an amount as currency.
     *
     * @param amount the amount to format
     * @return formatted amount string
     */
    private static String formatAmount(double amount) {
        return org.example.utils.ValidationUtils.formatAmount(amount);
    }
}

