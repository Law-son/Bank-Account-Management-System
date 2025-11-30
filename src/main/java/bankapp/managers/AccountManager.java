package main.java.bankapp.managers;

import main.java.bankapp.models.accounts.Account;
import main.java.bankapp.models.accounts.CheckingAccount;
import main.java.bankapp.models.accounts.SavingsAccount;
import main.java.bankapp.utils.InputValidator;

import java.util.Objects;

public class AccountManager {
    private Account[] accounts = new Account[50];
    private int accountCount = 0;

    public void addAccount(Account account) {
        addAccount(account, false);
    }

    public void addAccount(Account account, boolean silent) {
        if (accountCount < accounts.length) {
            accounts[accountCount++] = account;

            if (!silent) {
                if (account instanceof SavingsAccount) {
                    SavingsAccount savingsAccount = (SavingsAccount) account;
                    System.out.println("\nAccount created successfully!");
                    System.out.printf("Account Number: %s%n", account.getAccountNumber());
                    System.out.printf("Customer: %s (%s)%n", account.getCustomer().getName(), account.getCustomer().getCustomerType());
                    System.out.printf("Account Type: %s%n", account.getAccountType());
                    System.out.printf("Initial Balance: %s%n", InputValidator.formatAmount(account.getBalance()));
                    System.out.printf("Interest Rate: %.1f%%%n", savingsAccount.getInterestRate());
                    System.out.printf("Minimum Balance: %s%n", InputValidator.formatAmount(savingsAccount.getMinimumBalance()));
                    System.out.printf("Status: %s%n\n", account.getStatus());
                } else if (account instanceof CheckingAccount) {
                    CheckingAccount checkingAccount = (CheckingAccount) account;
                    System.out.println("\nAccount created successfully!");
                    System.out.printf("Account Number: %s%n", account.getAccountNumber());
                    System.out.printf("Customer: %s (%s)%n", account.getCustomer().getName(), account.getCustomer().getCustomerType());
                    System.out.printf("Account Type: %s%n", account.getAccountType());
                    System.out.printf("Initial Balance: %s%n", InputValidator.formatAmount(account.getBalance()));
                    System.out.printf("Overdrift Limit: %s%n", InputValidator.formatAmount(checkingAccount.getOverdraftLimit()));
                    System.out.printf("Monthly Fee: %s WAIVED - Premium Customer%n", InputValidator.formatAmount(0.0));
                    System.out.printf("Status: %s%n\n", account.getStatus());
                }
            }

        } else {
            if (!silent) {
                System.out.println("Error: Account storage is full.");
            }
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
        System.out.println("ACCOUNT LISTING");
        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf(" ACC NO | %-18s | %-10s | %-15s | %s%n",
                "CUSTOMER NAME", "TYPE", "BALANCE", "STATUS");
        System.out.println("-------------------------------------------------------------------------------");
        for (int i = 0; i < accountCount; i++) {
            accounts[i].displayAccountDetails();
        }
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
}