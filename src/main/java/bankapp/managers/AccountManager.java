package main.java.bankapp.managers;

import main.java.bankapp.models.accounts.Account;
import main.java.bankapp.utils.InputValidator;

import java.util.Objects;

public class AccountManager {
    private Account[] accounts = new Account[50];
    private int accountCount = 0;

    public void addAccount(Account account) {
        if (accountCount < accounts.length) {
            accounts[accountCount++] = account;

            if (Objects.equals(account.getAccountType(), "Regular")) {
                System.out.println("\nAccount created successfully!");
                System.out.printf("Account Number: %s%n", account.getAccountNumber());
                System.out.printf("Customer: %s (%s)%n", account.getCustomer().getName(), account.getCustomer().getCustomerType());
                System.out.printf("Account Type: %s%n", account.getAccountType());
                System.out.printf("Initial Balance: %s%n", InputValidator.formatAmount(account.getBalance()));
                System.out.printf("Interest Rate: 3.5%%%n");
                System.out.printf("Minimum Balance: %s%n", InputValidator.formatAmount(500.0));
                System.out.printf("Status: Active%n\n");
            } else {
                System.out.println("\nAccount created successfully!");
                System.out.printf("Account Number: %s%n", account.getAccountNumber());
                System.out.printf("Customer: %s (%s)%n", account.getCustomer().getName(), account.getCustomer().getCustomerType());
                System.out.printf("Account Type: %s%n", account.getAccountType());
                System.out.printf("Initial Balance: %s%n", InputValidator.formatAmount(account.getBalance()));
                System.out.printf("Overdrift Limit: %s%n", InputValidator.formatAmount(1000.0));
                System.out.printf("Monthly Fee: %s WAIVED - Premium Customer%n", InputValidator.formatAmount(0.0));
                System.out.printf("Status: Active%n\n");
            }

        } else {
            System.out.println("Error: Account storage is full.");
        }
    }

    public Account findAccount(String accountNumber) {
        // using linear search
        for (int i = 0; i < accountCount; i++) {
            if (accounts[i].getAccountNumber().equals(accountNumber)) {
                return accounts[i];
            }
        }
        return null;
    }

    public void viewAllAccounts() {
        if (accountCount == 0) {
            System.out.println("No accounts found.");
            return;
        }
        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf(" ACC NO | %-18s | %-10s | %-15s | %s%n",
                "CUSTOMER NAME", "TYPE", "BALANCE", "STATUS");
        System.out.println("-------------------------------------------------------------------------------");
        for (int i = 0; i < accountCount; i++) {
            accounts[i].displayAccountDetails();
        }
        // Summary
        System.out.println("Total Accounts: " + accountCount);
        System.out.println("Total Bank Balance: " + InputValidator.formatAmount(getTotalBalance()));
    }

    public double getTotalBalance() {
        double total = 0;
        for (int i = 0; i < accountCount; i++) {
            total += accounts[i].getBalance();
        }
        return total;
    }

    // Helper to check array capacity
    public boolean hasCapacity() {
        return accountCount < accounts.length;
    }
}