package org.example.services;

import org.example.models.Account;
import org.example.models.CheckingAccount;
import org.example.models.Customer;
import org.example.models.RegularCustomer;
import org.example.models.SavingsAccount;
import org.example.models.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for TransactionManager.
 * Tests transaction recording and retrieval.
 */
class TransactionManagerTest {
    private TransactionManager transactionManager;
    private Account testAccount;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        transactionManager = new TransactionManager();
        testCustomer = new RegularCustomer("Test Customer", 30, "+1-555-0000", "Test Address");
        testAccount = new SavingsAccount(testCustomer, 1000.0);
    }

    @Test
    void addTransactionRecordsTransaction() {
        // Arrange
        Transaction transaction = new Transaction(
                testAccount.getAccountNumber(),
                "Deposit",
                500.0,
                1500.0
        );

        // Act
        transactionManager.addTransaction(transaction);

        // Assert
        assertEquals(1, transactionManager.getTransactionCount(),
                "Transaction count should be 1 after adding one transaction");
    }

    @Test
    void getTransactionsByAccountReturnsCorrectTransactions() {
        // Arrange
        String accountNumber = testAccount.getAccountNumber();
        Transaction transaction1 = new Transaction(accountNumber, "Deposit", 500.0, 1500.0);
        Transaction transaction2 = new Transaction(accountNumber, "Withdrawal", 200.0, 1300.0);
        
        // Create another account for different transactions
        Account otherAccount = new CheckingAccount(testCustomer, 500.0);
        Transaction otherTransaction = new Transaction(
                otherAccount.getAccountNumber(),
                "Deposit",
                100.0,
                600.0
        );

        // Act
        transactionManager.addTransaction(transaction1);
        transactionManager.addTransaction(transaction2);
        transactionManager.addTransaction(otherTransaction);
        
        var accountTransactions = transactionManager.getTransactionsByAccount(accountNumber);

        // Assert
        assertEquals(2, accountTransactions.size(),
                "Should return 2 transactions for the test account");
        assertEquals(transaction1.getAccountNumber(), accountTransactions.get(0).getAccountNumber(),
                "First transaction should belong to test account");
        assertEquals(transaction2.getAccountNumber(), accountTransactions.get(1).getAccountNumber(),
                "Second transaction should belong to test account");
    }

    @Test
    void transactionRecordsCorrectDetails() {
        // Arrange
        String accountNumber = testAccount.getAccountNumber();
        String transactionType = "Deposit";
        double amount = 500.0;
        double balance = 1500.0;
        Transaction transaction = new Transaction(accountNumber, transactionType, amount, balance);

        // Act
        transactionManager.addTransaction(transaction);
        var transactions = transactionManager.getTransactionsByAccount(accountNumber);

        // Assert
        assertEquals(1, transactions.size(), "Should have one transaction");
        assertEquals(accountNumber, transactions.get(0).getAccountNumber(),
                "Transaction should have correct account number");
        assertEquals(transactionType, transactions.get(0).getType(),
                "Transaction should have correct type");
        assertEquals(amount, transactions.get(0).getAmount(), 0.01,
                "Transaction should have correct amount");
        assertEquals(balance, transactions.get(0).getBalance(), 0.01,
                "Transaction should have correct balance");
    }

    @Test
    void multipleTransactionsForSameAccount() {
        // Arrange
        String accountNumber = testAccount.getAccountNumber();
        Transaction transaction1 = new Transaction(accountNumber, "Deposit", 100.0, 1100.0);
        Transaction transaction2 = new Transaction(accountNumber, "Deposit", 200.0, 1300.0);
        Transaction transaction3 = new Transaction(accountNumber, "Withdrawal", 50.0, 1250.0);

        // Act
        transactionManager.addTransaction(transaction1);
        transactionManager.addTransaction(transaction2);
        transactionManager.addTransaction(transaction3);

        // Assert
        assertEquals(3, transactionManager.getTransactionCount(),
                "Should have 3 total transactions");
        
        var accountTransactions = transactionManager.getTransactionsByAccount(accountNumber);
        assertEquals(3, accountTransactions.size(),
                "Should return all 3 transactions for the account");
    }
}

