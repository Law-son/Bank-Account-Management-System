package org.example.models;

import org.example.models.exceptions.InsufficientFundsException;
import org.example.models.exceptions.InvalidAmountException;
import org.example.models.exceptions.OverdraftExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for Account deposit and withdraw methods.
 * Tests balance updates and exception conditions.
 */
class AccountTest {
    private Customer testCustomer;
    private SavingsAccount savingsAccount;
    private CheckingAccount checkingAccount;

    @BeforeEach
    void setUp() {
        // Reset account counter to ensure consistent account numbers
        Account.resetAccountCounter();
        
        testCustomer = new RegularCustomer("Test Customer", 30, "+1-555-0000", "Test Address");
        savingsAccount = new SavingsAccount(testCustomer, 1000.0);
        checkingAccount = new CheckingAccount(testCustomer, 1000.0);
    }

    @Test
    void depositUpdatesBalance() throws InvalidAmountException {
        // Arrange
        double initialBalance = savingsAccount.getBalance();
        double depositAmount = 500.0;

        // Act
        savingsAccount.deposit(depositAmount);

        // Assert
        assertEquals(initialBalance + depositAmount, savingsAccount.getBalance(), 0.01,
                "Balance should increase by deposit amount");
    }

    @Test
    void depositWithInvalidAmountThrowsException() {
        // Act & Assert
        assertThrows(InvalidAmountException.class, () -> savingsAccount.deposit(0),
                "Deposit with zero amount should throw InvalidAmountException");
        
        assertThrows(InvalidAmountException.class, () -> savingsAccount.deposit(-100),
                "Deposit with negative amount should throw InvalidAmountException");
    }

    @Test
    void withdrawUpdatesBalance() throws InsufficientFundsException, OverdraftExceededException {
        // Arrange
        double initialBalance = savingsAccount.getBalance();
        double withdrawAmount = 200.0;

        // Act
        boolean result = savingsAccount.withdraw(withdrawAmount);

        // Assert
        assertTrue(result, "Withdrawal should return true on success");
        assertEquals(initialBalance - withdrawAmount, savingsAccount.getBalance(), 0.01,
                "Balance should decrease by withdrawal amount");
    }

    @Test
    void withdrawBelowMinimumThrowsException() {
        // Arrange
        double withdrawAmount = savingsAccount.getBalance() - savingsAccount.getMinimumBalance() + 1;

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> savingsAccount.withdraw(withdrawAmount),
                "Withdrawal below minimum balance should throw InsufficientFundsException");
    }

    @Test
    void overdraftWithinLimitAllowed() throws InsufficientFundsException, OverdraftExceededException {
        // Arrange
        double initialBalance = checkingAccount.getBalance();
        double overdraftAmount = checkingAccount.getOverdraftLimit() / 2;
        double withdrawAmount = initialBalance + overdraftAmount;

        // Act
        boolean result = checkingAccount.withdraw(withdrawAmount);

        // Assert
        assertTrue(result, "Withdrawal within overdraft limit should succeed");
        assertEquals(initialBalance - withdrawAmount, checkingAccount.getBalance(), 0.01,
                "Balance should reflect overdraft amount");
    }

    @Test
    void overdraftExceedThrowsException() {
        // Arrange
        double initialBalance = checkingAccount.getBalance();
        double overdraftLimit = checkingAccount.getOverdraftLimit();
        double withdrawAmount = initialBalance + overdraftLimit + 1;

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> checkingAccount.withdraw(withdrawAmount),
                "Withdrawal exceeding overdraft limit should throw InsufficientFundsException");
    }

    @Test
    void savingsAccountMaintainsMinimumBalance() throws InsufficientFundsException, OverdraftExceededException {
        // Arrange
        double minimumBalance = savingsAccount.getMinimumBalance();
        double withdrawAmount = savingsAccount.getBalance() - minimumBalance;

        // Act
        savingsAccount.withdraw(withdrawAmount);

        // Assert
        assertEquals(minimumBalance, savingsAccount.getBalance(), 0.01,
                "Balance should equal minimum balance after maximum withdrawal");
    }

    @Test
    void checkingAccountAllowsOverdraft() throws InsufficientFundsException, OverdraftExceededException {
        // Arrange
        double initialBalance = checkingAccount.getBalance();
        double overdraftAmount = checkingAccount.getOverdraftLimit();
        double withdrawAmount = initialBalance + overdraftAmount;

        // Act
        checkingAccount.withdraw(withdrawAmount);

        // Assert
        assertEquals(-overdraftAmount, checkingAccount.getBalance(), 0.01,
                "Balance should reflect maximum overdraft amount");
    }
}

