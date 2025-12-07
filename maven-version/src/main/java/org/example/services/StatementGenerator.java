package org.example.services;

import org.example.models.Account;
import org.example.models.Transaction;
import org.example.utils.ValidationUtils;

public class StatementGenerator {
    private TransactionManager transactionManager;

    public StatementGenerator(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void generateStatement(String accountNumber, Account account) {
        // Display account details if account is found
        if (account != null) {
            System.out.printf("\nAccount: %s - %s%n", account.getAccountNumber(), account.getCustomer().getName());
            System.out.printf("Account Type: %s%n", account.getAccountType());
            System.out.printf("Current Balance: %s%n", ValidationUtils.formatAmount(account.getBalance()));
        } else {
            System.out.printf("\nAccount: %s%n", accountNumber);
        }
        
        Transaction[] accountTransactions = transactionManager.getTransactionsByAccount(accountNumber);

        if (accountTransactions.length == 0) {
            System.out.println("\n------------------------------------------");
            System.out.println("No transactions recorded for this account.");
            System.out.println("------------------------------------------\n");
        } else {
            printSummary(accountNumber, accountTransactions);
        }
    }

    private void printSummary(String accountNumber, Transaction[] accountTransactions) {
        double deposits = 0;
        double withdrawals = 0;
        int txnTotal = accountTransactions.length;

        System.out.println();
        System.out.println("TRANSACTION HISTORY");
        System.out.println("------------------------------------------------------------------------");
        System.out.printf("%-8s | %-20s | %-10s | %-10s | %-10s%n",
                "TXN ID", "DATE/TIME", "TYPE", "AMOUNT", "BALANCE");
        System.out.println("------------------------------------------------------------------------");

        for (Transaction transaction : accountTransactions) {
            // Format amount with sign before dollar sign: +$2,694.00 or -$2,694.00
            String sign = transaction.getAmount() >= 0 ? "+" : "-";
            String formattedAmount = ValidationUtils.formatAmount(Math.abs(transaction.getAmount()));
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
                    ValidationUtils.formatAmount(transaction.getBalance()));
        }

        System.out.println("------------------------------------------------------------------------");
        System.out.println();
        System.out.printf("Total Transactions: %d%n", txnTotal);
        System.out.printf("Total Deposits: %s%n", ValidationUtils.formatAmount(deposits));
        System.out.printf("Total Withdrawals: %s%n", ValidationUtils.formatAmount(withdrawals));
        System.out.printf("Net Change: %s%s%n", (deposits - withdrawals >= 0 ? "+" : ""), ValidationUtils.formatAmount(Math.abs(deposits - withdrawals)));
        System.out.println();
    }
}

