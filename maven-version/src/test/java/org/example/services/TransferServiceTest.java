package org.example.services;

import org.example.models.Account;
import org.example.models.CheckingAccount;
import org.example.models.Customer;
import org.example.models.RegularCustomer;
import org.example.models.SavingsAccount;
import org.example.models.Transaction;
import org.example.models.exceptions.AccountNotFoundException;
import org.example.models.exceptions.InsufficientFundsException;
import org.example.models.exceptions.InvalidAmountException;
import org.example.models.exceptions.OverdraftExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for TransferService.
 * Tests transfer functionality, balance updates, and exception conditions.
 */
class TransferServiceTest {
    private AccountManager accountManager;
    private TransactionManager transactionManager;
    private TransferService transferService;
    private Customer testCustomer;
    private SavingsAccount sourceAccount;
    private SavingsAccount destinationAccount;

    @BeforeEach
    void setUp() {
        // Reset account counter
        Account.resetAccountCounter();
        
        accountManager = new AccountManager();
        transactionManager = new TransactionManager();
        transferService = new TransferService(accountManager, transactionManager);
        
        testCustomer = new RegularCustomer("Test Customer", 30, "+1-555-0000", "Test Address");
        sourceAccount = new SavingsAccount(testCustomer, 2000.0);
        destinationAccount = new SavingsAccount(testCustomer, 1000.0);
        
        accountManager.addAccount(sourceAccount);
        accountManager.addAccount(destinationAccount);
    }

    @Test
    void transferUpdatesBothAccountBalances() throws AccountNotFoundException, InvalidAmountException, 
            InsufficientFundsException, OverdraftExceededException {
        // Arrange
        double transferAmount = 500.0;
        double sourceInitialBalance = sourceAccount.getBalance();
        double destinationInitialBalance = destinationAccount.getBalance();

        // Act
        transferService.transfer(sourceAccount.getAccountNumber(), destinationAccount.getAccountNumber(), transferAmount);

        // Assert
        assertEquals(sourceInitialBalance - transferAmount, sourceAccount.getBalance(), 0.01,
                "Source account balance should decrease by transfer amount");
        assertEquals(destinationInitialBalance + transferAmount, destinationAccount.getBalance(), 0.01,
                "Destination account balance should increase by transfer amount");
    }

    @Test
    void transferRecordsTransactions() throws AccountNotFoundException, InvalidAmountException,
            InsufficientFundsException, OverdraftExceededException {
        // Arrange
        double transferAmount = 300.0;
        int initialTransactionCount = transactionManager.getTransactionCount();

        // Act
        transferService.transfer(sourceAccount.getAccountNumber(), destinationAccount.getAccountNumber(), transferAmount);

        // Assert
        assertEquals(initialTransactionCount + 2, transactionManager.getTransactionCount(),
                "Should record 2 transactions (Transfer Out and Transfer In)");
        
        var sourceTransactions = transactionManager.getTransactionsByAccount(sourceAccount.getAccountNumber());
        var destinationTransactions = transactionManager.getTransactionsByAccount(destinationAccount.getAccountNumber());
        
        boolean hasTransferOut = sourceTransactions.stream()
                .anyMatch(txn -> "Transfer Out".equals(txn.getType()) && 
                        Math.abs(txn.getAmount() - transferAmount) < 0.01);
        
        boolean hasTransferIn = destinationTransactions.stream()
                .anyMatch(txn -> "Transfer In".equals(txn.getType()) && 
                        Math.abs(txn.getAmount() - transferAmount) < 0.01);
        
        assertTrue(hasTransferOut, "Source account should have Transfer Out transaction");
        assertTrue(hasTransferIn, "Destination account should have Transfer In transaction");
    }

    @Test
    void transferWithInsufficientFundsThrowsException() {
        // Arrange
        double transferAmount = sourceAccount.getBalance() + 1000.0; // More than available

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () ->
                transferService.transfer(sourceAccount.getAccountNumber(), destinationAccount.getAccountNumber(), transferAmount),
                "Transfer with insufficient funds should throw InsufficientFundsException");
    }

    @Test
    void transferWithInvalidAmountThrowsException() {
        // Act & Assert
        assertThrows(InvalidAmountException.class, () ->
                transferService.transfer(sourceAccount.getAccountNumber(), destinationAccount.getAccountNumber(), 0),
                "Transfer with zero amount should throw InvalidAmountException");
        
        assertThrows(InvalidAmountException.class, () ->
                transferService.transfer(sourceAccount.getAccountNumber(), destinationAccount.getAccountNumber(), -100),
                "Transfer with negative amount should throw InvalidAmountException");
    }

    @Test
    void transferToNonExistentAccountThrowsException() {
        // Act & Assert
        assertThrows(AccountNotFoundException.class, () ->
                transferService.transfer(sourceAccount.getAccountNumber(), "INVALID", 100),
                "Transfer to non-existent account should throw AccountNotFoundException");
    }

    @Test
    void transferFromNonExistentAccountThrowsException() {
        // Act & Assert
        assertThrows(AccountNotFoundException.class, () ->
                transferService.transfer("INVALID", destinationAccount.getAccountNumber(), 100),
                "Transfer from non-existent account should throw AccountNotFoundException");
    }

    @Test
    void transferToSameAccountThrowsException() {
        // Act & Assert
        assertThrows(InvalidAmountException.class, () ->
                transferService.transfer(sourceAccount.getAccountNumber(), sourceAccount.getAccountNumber(), 100),
                "Transfer to same account should throw InvalidAmountException");
    }

    @Test
    void transferWithCheckingAccountOverdraft() throws AccountNotFoundException, InvalidAmountException,
            InsufficientFundsException, OverdraftExceededException {
        // Arrange
        CheckingAccount checkingSource = new CheckingAccount(testCustomer, 500.0);
        accountManager.addAccount(checkingSource);
        
        double transferAmount = 1200.0; // Within overdraft limit (500 + 1000 = 1500)

        // Act
        transferService.transfer(checkingSource.getAccountNumber(), destinationAccount.getAccountNumber(), transferAmount);

        // Assert
        assertEquals(500.0 - transferAmount, checkingSource.getBalance(), 0.01,
                "Checking account should allow overdraft within limit");
        assertEquals(1000.0 + transferAmount, destinationAccount.getBalance(), 0.01,
                "Destination account should receive the transfer");
    }
}

