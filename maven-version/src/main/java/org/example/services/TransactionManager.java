package org.example.services;

import org.example.models.Transaction;

/**
 * Service class responsible for managing transaction records.
 * Handles transaction storage and retrieval operations.
 */
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

    public Transaction[] getTransactionsByAccount(String accountNumber) {
        Transaction[] accountTransactions = new Transaction[transactionCount];
        int count = 0;
        
        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].getAccountNumber().equals(accountNumber)) {
                accountTransactions[count++] = transactions[i];
            }
        }
        
        // Return array with exact size
        Transaction[] result = new Transaction[count];
        System.arraycopy(accountTransactions, 0, result, 0, count);
        return result;
    }

    public int getTransactionCount() {
        return transactionCount;
    }
}

