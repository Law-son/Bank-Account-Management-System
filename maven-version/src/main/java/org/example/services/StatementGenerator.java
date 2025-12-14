package org.example.services;

import org.example.models.Account;
import org.example.models.Transaction;
import org.example.utils.ValidationUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class responsible for generating account statements.
 * Uses Streams and functional programming for data processing.
 * Formats and displays transaction history for a given account.
 */
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
        
        // Get transactions sorted by date (newest first) using Streams
        List<Transaction> accountTransactions = transactionManager.getTransactionsByAccountSortedByDate(accountNumber);

        if (accountTransactions.isEmpty()) {
            System.out.println("\n------------------------------------------");
            System.out.println("No transactions recorded for this account.");
            System.out.println("------------------------------------------\n");
        } else {
            printSummary(accountNumber, accountTransactions);
        }
    }

    /**
     * Prints transaction summary using Streams for aggregation.
     *
     * @param accountNumber the account number
     * @param accountTransactions list of transactions to display
     */
    private void printSummary(String accountNumber, List<Transaction> accountTransactions) {
        // Use Streams for aggregation
        double deposits = accountTransactions.stream()
                .filter(txn -> txn.getType().equalsIgnoreCase("Deposit") || 
                              txn.getType().equalsIgnoreCase("Transfer In"))
                .mapToDouble(Transaction::getAmount)
                .sum();
        
        double withdrawals = accountTransactions.stream()
                .filter(txn -> txn.getType().equalsIgnoreCase("Withdrawal") || 
                              txn.getType().equalsIgnoreCase("Transfer Out"))
                .mapToDouble(txn -> Math.abs(txn.getAmount()))
                .sum();
        
        int txnTotal = accountTransactions.size();

        System.out.println();
        System.out.println("TRANSACTION HISTORY");
        System.out.println("------------------------------------------------------------------------");
        System.out.printf("%-8s | %-20s | %-10s | %-10s | %-10s%n",
                "TXN ID", "DATE/TIME", "TYPE", "AMOUNT", "BALANCE");
        System.out.println("------------------------------------------------------------------------");

        // Use Streams for iteration and transformation
        accountTransactions.forEach(transaction -> {
            // Format amount with sign before dollar sign: +$2,694.00 or -$2,694.00
            String sign = transaction.getAmount() >= 0 ? "+" : "-";
            String formattedAmount = ValidationUtils.formatAmount(Math.abs(transaction.getAmount()));
            // Remove the $ from formatAmount and add it after the sign
            String amountStr = sign + "$" + formattedAmount.substring(1);

            // Print each row
            System.out.printf("%-8s | %-20s | %-10s | %-10s | %s%n",
                    transaction.getTransactionID(),
                    transaction.getDateTime(),
                    transaction.getType().toUpperCase(),
                    amountStr,
                    ValidationUtils.formatAmount(transaction.getBalance()));
        });

        System.out.println("------------------------------------------------------------------------");
        System.out.println();
        System.out.printf("Total Transactions: %d%n", txnTotal);
        System.out.printf("Total Deposits: %s%n", ValidationUtils.formatAmount(deposits));
        System.out.printf("Total Withdrawals: %s%n", ValidationUtils.formatAmount(withdrawals));
        System.out.printf("Net Change: %s%s%n", (deposits - withdrawals >= 0 ? "+" : ""), ValidationUtils.formatAmount(Math.abs(deposits - withdrawals)));
        System.out.println();
    }
}

