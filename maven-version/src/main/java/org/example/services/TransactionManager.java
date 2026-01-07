package org.example.services;

import org.example.models.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class responsible for managing transaction records.
 * Uses ArrayList for dynamic storage and Streams for efficient filtering and sorting.
 * Implements functional programming patterns with lambdas and comparators.
 */
public class TransactionManager {
    private final List<Transaction> transactions = Collections.synchronizedList(new ArrayList<>());

    /**
     * Adds a transaction to the manager.
     * Thread-safe for concurrent access.
     *
     * @param transaction the transaction to add
     */
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    /**
     * Gets all transactions for a specific account using Streams.
     *
     * @param accountNumber the account number to filter by
     * @return list of transactions for the account
     */
    public List<Transaction> getTransactionsByAccount(String accountNumber) {
        return transactions.stream()
                .filter(txn -> txn.getAccountNumber().equals(accountNumber))
                .collect(Collectors.toList());
    }

    /**
     * Gets all transactions for a specific account, sorted by date (newest first).
     *
     * @param accountNumber the account number to filter by
     * @return sorted list of transactions
     */
    public List<Transaction> getTransactionsByAccountSortedByDate(String accountNumber) {
        return transactions.stream()
                .filter(txn -> txn.getAccountNumber().equals(accountNumber))
                .sorted(Comparator.comparing(Transaction::getDateTimeAsLocalDateTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Gets all transactions for a specific account, sorted by amount (highest first).
     *
     * @param accountNumber the account number to filter by
     * @return sorted list of transactions
     */
    public List<Transaction> getTransactionsByAccountSortedByAmount(String accountNumber) {
        return transactions.stream()
                .filter(txn -> txn.getAccountNumber().equals(accountNumber))
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Gets all transactions using Streams.
     *
     * @return list of all transactions
     */
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions);
    }

    /**
     * Gets transactions filtered by type using Streams and lambda.
     *
     * @param transactionType the type to filter by (e.g., "Deposit", "Withdrawal")
     * @return list of filtered transactions
     */
    public List<Transaction> getTransactionsByType(String transactionType) {
        return transactions.stream()
                .filter(txn -> txn.getType().equalsIgnoreCase(transactionType))
                .collect(Collectors.toList());
    }

    /**
     * Gets the total count of transactions.
     *
     * @return the number of transactions
     */
    public int getTransactionCount() {
        return transactions.size();
    }

    /**
     * Calculates total amount for a specific account using Streams and aggregation.
     *
     * @param accountNumber the account number
     * @return total transaction amount for the account
     */
    public double getTotalAmountByAccount(String accountNumber) {
        return transactions.stream()
                .filter(txn -> txn.getAccountNumber().equals(accountNumber))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /**
     * Gets transaction count for a specific account using Streams.
     *
     * @param accountNumber the account number
     * @return count of transactions for the account
     */
    public long getTransactionCountByAccount(String accountNumber) {
        return transactions.stream()
                .filter(txn -> txn.getAccountNumber().equals(accountNumber))
                .count();
    }
}

