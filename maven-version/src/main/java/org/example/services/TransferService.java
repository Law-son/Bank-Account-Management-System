package org.example.services;

import org.example.models.Account;
import org.example.models.Transaction;
import org.example.models.exceptions.AccountNotFoundException;
import org.example.models.exceptions.InsufficientFundsException;
import org.example.models.exceptions.InvalidAmountException;
import org.example.models.exceptions.OverdraftExceededException;

/**
 * Service class responsible for handling money transfers between accounts.
 * Follows Single Responsibility Principle by isolating transfer logic.
 */
public class TransferService {
    private final AccountManager accountManager;
    private final TransactionManager transactionManager;

    /**
     * Constructs a TransferService with the required dependencies.
     *
     * @param accountManager the account manager to find accounts
     * @param transactionManager the transaction manager to record transactions
     */
    public TransferService(AccountManager accountManager, TransactionManager transactionManager) {
        this.accountManager = accountManager;
        this.transactionManager = transactionManager;
    }

    /**
     * Transfers money from one account to another.
     *
     * @param fromAccountNumber the account number to transfer from
     * @param toAccountNumber the account number to transfer to
     * @param amount the amount to transfer
     * @throws AccountNotFoundException if either account is not found
     * @throws InvalidAmountException if the amount is invalid
     * @throws InsufficientFundsException if the source account has insufficient funds
     * @throws OverdraftExceededException if withdrawal exceeds overdraft limit
     */
    public void transfer(String fromAccountNumber, String toAccountNumber, double amount)
            throws AccountNotFoundException, InvalidAmountException, InsufficientFundsException, OverdraftExceededException {
        // Validate amount
        if (amount <= 0) {
            throw new InvalidAmountException("Invalid amount. Amount must be greater than 0.");
        }

        // Find source account
        Account fromAccount = accountManager.findAccount(fromAccountNumber);
        if (fromAccount == null) {
            throw new AccountNotFoundException("Account not found. Please check the account number.");
        }

        // Find destination account
        Account toAccount = accountManager.findAccount(toAccountNumber);
        if (toAccount == null) {
            throw new AccountNotFoundException("Account not found. Please check the account number.");
        }

        // Prevent self-transfer
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new InvalidAmountException("Cannot transfer to the same account.");
        }

        // Withdraw from source account (this will throw InsufficientFundsException or OverdraftExceededException if needed)
        fromAccount.withdraw(amount);

        // Deposit to destination account (this will throw InvalidAmountException if needed)
        // Note: If deposit fails after successful withdrawal, we would need to reverse the withdrawal
        // However, deposit should never fail with a valid amount, so this is a safety check
        toAccount.deposit(amount);

        // Record transactions
        Transaction withdrawalTransaction = new Transaction(
                fromAccountNumber,
                "Transfer Out",
                amount,
                fromAccount.getBalance()
        );
        transactionManager.addTransaction(withdrawalTransaction);

        Transaction depositTransaction = new Transaction(
                toAccountNumber,
                "Transfer In",
                amount,
                toAccount.getBalance()
        );
        transactionManager.addTransaction(depositTransaction);
    }
}

