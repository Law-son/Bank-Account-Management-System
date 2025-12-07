package org.example.models.transactions;

import org.example.models.accounts.Account;
import org.example.utils.InputValidator;

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
            System.out.printf("Current Balance: %s%n", InputValidator.formatAmount(account.getBalance()));
        } else {
            System.out.printf("\nAccount: %s%n", accountNumber);
        }
        
        boolean found = false;

        // Display in reverse chronological order (Newest first)
        for (int i = transactionCount - 1; i >= 0; i--) {
            if (transactions[i].getAccountNumber().equals(accountNumber)) {
                found = true;
                break;
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
        int txnTotal = 0;

        System.out.println();
        System.out.println("TRANSACTION HISTORY");
        System.out.println("------------------------------------------------------------------------");
        System.out.printf("%-8s | %-20s | %-10s | %-10s | %-10s%n",
                "TXN ID", "DATE/TIME", "TYPE", "AMOUNT", "BALANCE");
        System.out.println("------------------------------------------------------------------------");

        for (int i = 0; i < transactionCount; i++) {
            Transaction transaction = transactions[i];

            if (transaction.getAccountNumber().equals(accountNumber)) {
                txnTotal++;

                // Format amount with sign before dollar sign: +$2,694.00 or -$2,694.00
                String sign = transaction.getAmount() >= 0 ? "+" : "-";
                String formattedAmount = InputValidator.formatAmount(Math.abs(transaction.getAmount()));
                // Remove the $ from formatAmount and add it after the sign
                String amountStr = sign + "$" + formattedAmount.substring(1);

                // Sum totals
                if (transaction.getType().equalsIgnoreCase("DEPOSIT")) {
                    deposits += transaction.getAmount();
                } else if (transaction.getType().equalsIgnoreCase("WITHDRAWAL")) {
                    withdrawals += Math.abs(transaction.getAmount());
                }

                // Print each row
                System.out.printf("%-8s | %-20s | %-10s | %-10s | %s%n",
                        transaction.getTransactionID(),
                        transaction.getDateTime(),
                        transaction.getType().toUpperCase(),
                        amountStr,
                        InputValidator.formatAmount(transaction.getBalance()));
            }
        }

        System.out.println("------------------------------------------------------------------------");
        System.out.println();
        System.out.printf("Total Transactions: %d%n", txnTotal);
        System.out.printf("Total Deposits: %s%n", InputValidator.formatAmount(deposits));
        System.out.printf("Total Withdrawals: %s%n", InputValidator.formatAmount(withdrawals));
        System.out.printf("Net Change: %s%s%n", (deposits - withdrawals >= 0 ? "+" : ""), InputValidator.formatAmount(Math.abs(deposits - withdrawals)));
        System.out.println();
    }
}