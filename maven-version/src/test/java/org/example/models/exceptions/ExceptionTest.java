package org.example.models.exceptions;

import org.example.models.Account;
import org.example.models.CheckingAccount;
import org.example.models.Customer;
import org.example.models.RegularCustomer;
import org.example.models.SavingsAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for exception handling.
 * Tests that exceptions are thrown correctly for invalid operations.
 */
class ExceptionTest {
    private Customer testCustomer;
    private SavingsAccount savingsAccount;
    private CheckingAccount checkingAccount;

    @BeforeEach
    void setUp() {
        Account.resetAccountCounter();
        testCustomer = new RegularCustomer("Test Customer", 30, "+1-555-0000", "Test Address");
        savingsAccount = new SavingsAccount(testCustomer, 1000.0);
        checkingAccount = new CheckingAccount(testCustomer, 1000.0);
    }

    @Test
    void invalidAmountExceptionOnZeroDeposit() {
        // Act & Assert
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () ->
                savingsAccount.deposit(0),
                "Deposit of zero should throw InvalidAmountException");
        
        assertTrue(exception.getMessage().contains("Invalid amount"),
                "Exception message should indicate invalid amount");
    }

    @Test
    void invalidAmountExceptionOnNegativeDeposit() {
        // Act & Assert
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () ->
                savingsAccount.deposit(-100),
                "Deposit of negative amount should throw InvalidAmountException");
        
        assertTrue(exception.getMessage().contains("Invalid amount"),
                "Exception message should indicate invalid amount");
    }

    @Test
    void insufficientFundsExceptionOnSavingsWithdrawal() {
        // Arrange
        double withdrawAmount = savingsAccount.getBalance() - savingsAccount.getMinimumBalance() + 1;

        // Act & Assert
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () ->
                savingsAccount.withdraw(withdrawAmount),
                "Withdrawal below minimum balance should throw InsufficientFundsException");
        
        assertTrue(exception.getMessage().contains("Insufficient funds"),
                "Exception message should indicate insufficient funds");
    }

    @Test
    void insufficientFundsExceptionOnCheckingOverdraftExceed() {
        // Arrange
        double withdrawAmount = checkingAccount.getBalance() + checkingAccount.getOverdraftLimit() + 1;

        // Act & Assert
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () ->
                checkingAccount.withdraw(withdrawAmount),
                "Withdrawal exceeding overdraft limit should throw InsufficientFundsException");
        
        assertTrue(exception.getMessage().contains("Insufficient funds"),
                "Exception message should indicate insufficient funds");
    }

    @Test
    void accountNotFoundExceptionHasCorrectMessage() {
        // Act
        AccountNotFoundException exception = new AccountNotFoundException("Account not found. Please check the account number.");

        // Assert
        assertEquals("Account not found. Please check the account number.", exception.getMessage(),
                "Exception should have correct message");
    }

    @Test
    void invalidAmountExceptionHasCorrectMessage() {
        // Act
        InvalidAmountException exception = new InvalidAmountException("Invalid amount. Amount must be greater than 0.");

        // Assert
        assertEquals("Invalid amount. Amount must be greater than 0.", exception.getMessage(),
                "Exception should have correct message");
    }

    @Test
    void insufficientFundsExceptionHasCorrectMessage() {
        // Act
        InsufficientFundsException exception = new InsufficientFundsException(
                "Transaction Failed: Insufficient funds. Current balance: $1,000.00");

        // Assert
        assertTrue(exception.getMessage().contains("Insufficient funds"),
                "Exception message should contain 'Insufficient funds'");
        assertTrue(exception.getMessage().contains("Current balance"),
                "Exception message should contain balance information");
    }
}

