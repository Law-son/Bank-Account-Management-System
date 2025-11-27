package main.java.bankapp.managers;

import main.java.bankapp.models.accounts.Account;

public class AccountManager {
    private Account[] accounts = new Account[50];
    private int accountCount = 0;

    public void addAccount(Account account) {
        if (accountCount < accounts.length) {
            accounts[accountCount++] = account;
            System.out.println("Account created successfully: " + account.getAccountNumber());
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
        System.out.println("\n--- All Accounts ---");
        for (int i = 0; i < accountCount; i++) {
            accounts[i].displayAccountDetails();
        }
        System.out.println("Total Accounts: " + accountCount);
        System.out.println("Total Bank Balance: $" + getTotalBalance());
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