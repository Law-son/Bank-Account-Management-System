package main.java.bankapp.models.transactions;

import main.java.bankapp.models.accounts.Account;

public class TransactionManager {
    private Transaction[] transactions = new Transaction[200];
    private int transactionCount = 0;

    public void addTransaction(Transaction transaction) {
        if (transactionCount < transactions.length) {
            transactions[transactionCount++] = transaction;
        } else {
            System.out.println("Transaction history full. Cannot record new log.");
        }
    }

    public void viewTransactionsByAccount(String accountNumber, Account account) {
        // Display account details if account is found
        if (account != null) {
            System.out.printf("\nAccount: %s - %s%n", account.getAccountNumber(), account.getCustomer().getName());
            System.out.printf("Account Type: %s%n", account.getAccountType());
            System.out.printf("Current Balance: $%.2f%n", account.getBalance());
        } else {
            System.out.printf("\nAccount: %s%n", accountNumber);
        }
        
        boolean found = false;

        // Display in reverse chronological order (Newest first)
        for (int i = transactionCount - 1; i >= 0; i--) {
            if (transactions[i].getAccountNumber().equals(accountNumber)) {
                transactions[i].displayTransactionDetails();
                found = true;
            }
        }

        if (!found) {
            System.out.println("\n------------------------------------------");
            System.out.println("No transactions recorded for this account.");
            System.out.println("------------------------------------------\n");
        } else {
            printSummary(accountNumber);
        }
    }

    private void printSummary(String accountNumber) {
        double deposits = 0;
        double withdrawals = 0;

        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].getAccountNumber().equals(accountNumber)) {
                if (transactions[i].getType().equalsIgnoreCase("Deposit")) {
                    deposits += transactions[i].getAmount();
                } else {
                    withdrawals += transactions[i].getAmount();
                }
            }
        }
        System.out.println("Summary: Total Deposits: $" + deposits + " | Total Withdrawals: $" + withdrawals);
    }
}