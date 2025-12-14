package org.example.services;

import org.example.models.Account;
import org.example.models.CheckingAccount;
import org.example.models.SavingsAccount;
import org.example.utils.ValidationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class responsible for managing bank accounts.
 * Uses HashMap for efficient O(1) account lookup by account number.
 * Implements functional programming patterns with Streams and lambdas.
 */
public class AccountManager {
    private final Map<String, Account> accounts = new HashMap<>();

    /**
     * Adds an account to the manager.
     *
     * @param account the account to add
     */
    public void addAccount(Account account) {
        addAccount(account, false);
    }

    /**
     * Adds an account to the manager with optional silent mode.
     *
     * @param account the account to add
     * @param silent  if true, suppresses success messages
     */
    public void addAccount(Account account, boolean silent) {
        accounts.put(account.getAccountNumber(), account);

        if (!silent) {
            if (account instanceof SavingsAccount) {
                SavingsAccount savingsAccount = (SavingsAccount) account;
                System.out.println("\nAccount created successfully!");
                System.out.printf("Account Number: %s%n", account.getAccountNumber());
                System.out.printf("Customer: %s (%s)%n", account.getCustomer().getName(), account.getCustomer().getCustomerType());
                System.out.printf("Account Type: %s%n", account.getAccountType());
                System.out.printf("Initial Balance: %s%n", ValidationUtils.formatAmount(account.getBalance()));
                System.out.printf("Interest Rate: %.1f%%%n", savingsAccount.getInterestRate());
                System.out.printf("Minimum Balance: %s%n", ValidationUtils.formatAmount(savingsAccount.getMinimumBalance()));
                System.out.printf("Status: %s%n\n", account.getStatus());
            } else if (account instanceof CheckingAccount) {
                CheckingAccount checkingAccount = (CheckingAccount) account;
                System.out.println("\nAccount created successfully!");
                System.out.printf("Account Number: %s%n", account.getAccountNumber());
                System.out.printf("Customer: %s (%s)%n", account.getCustomer().getName(), account.getCustomer().getCustomerType());
                System.out.printf("Account Type: %s%n", account.getAccountType());
                System.out.printf("Initial Balance: %s%n", ValidationUtils.formatAmount(account.getBalance()));
                System.out.printf("Overdrift Limit: %s%n", ValidationUtils.formatAmount(checkingAccount.getOverdraftLimit()));
                System.out.printf("Monthly Fee: %s (WAIVED - Premium Customer)%n", ValidationUtils.formatAmount(0.0));
                System.out.printf("Status: %s%n\n", account.getStatus());
            }
        }
    }

    /**
     * Finds an account by account number using HashMap lookup (O(1) complexity).
     *
     * @param accountNumber the account number to search for
     * @return the account if found, null otherwise
     */
    public Account findAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    /**
     * Displays all accounts using Streams for iteration.
     */
    public void viewAllAccounts() {
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }
        System.out.println("\nACCOUNT LISTING");
        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf(" ACC NO | %-18s | %-10s | %-15s | %s%n",
                "CUSTOMER NAME", "TYPE", "BALANCE", "STATUS");
        System.out.println("-------------------------------------------------------------------------------");
        
        // Use Streams for iteration
        accounts.values().stream()
                .forEach(Account::displayAccountDetails);
        
        System.out.println("Total Accounts: " + accounts.size());
        System.out.println("Total Bank Balance: " + ValidationUtils.formatAmount(getTotalBalance()));
    }

    /**
     * Calculates total balance across all accounts using Streams and lambda.
     *
     * @return the sum of all account balances
     */
    public double getTotalBalance() {
        return accounts.values().stream()
                .mapToDouble(Account::getBalance)
                .sum();
    }
    
    /**
     * Gets all accounts as a collection.
     *
     * @return collection of all accounts
     */
    public Map<String, Account> getAllAccounts() {
        return new HashMap<>(accounts);
    }
    
    /**
     * Gets the count of accounts.
     *
     * @return the number of accounts
     */
    public int getAccountCount() {
        return accounts.size();
    }
}

