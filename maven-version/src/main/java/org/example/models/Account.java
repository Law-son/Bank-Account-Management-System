package org.example.models;

import org.example.models.exceptions.InvalidAmountException;
import org.example.models.exceptions.InsufficientFundsException;
import org.example.models.exceptions.OverdraftExceededException;
import org.example.models.interfaces.Transactable;

/**
 * Abstract base class representing a bank account.
 * Implements the Transactable interface for processing transactions.
 */
public abstract class Account implements Transactable {
    private String accountNumber;
    private Customer customer;
    protected double balance;
    private String status;

    protected static int accountCounter = 0;

    public Account(Customer customer, double initialDeposit) {
        this.accountNumber = "ACC" + String.format("%03d", ++accountCounter);
        this.customer = customer;
        this.balance = initialDeposit;
        this.status = "ACTIVE";
    }

    public String getAccountNumber() { return accountNumber; }
    public Customer getCustomer() { return customer; }
    public double getBalance() { return balance; }
    public String getStatus() { 
        // Format status for display: "ACTIVE" -> "Active"
        if (status != null && !status.isEmpty()) {
            return status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
        }
        return status;
    }

    /**
     * Deposits the specified amount into the account.
     * All subclasses use the same deposit logic, so this is implemented as a concrete method.
     *
     * @param amount the amount to deposit
     * @throws InvalidAmountException if the amount is less than or equal to zero
     */
    public void deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Invalid amount. Amount must be greater than 0.");
        }
        this.balance += amount;
    }

    /**
     * Withdraws the specified amount from the account.
     *
     * @param amount the amount to withdraw
     * @return true if withdrawal is successful
     * @throws InsufficientFundsException if there are insufficient funds
     * @throws OverdraftExceededException if withdrawal exceeds overdraft limit (for checking accounts)
     */
    /**
     * Withdraws the specified amount from the account.
     *
     * @param amount the amount to withdraw
     * @return true if withdrawal is successful
     * @throws InsufficientFundsException if there are insufficient funds
     * @throws OverdraftExceededException if withdrawal exceeds overdraft limit (for checking accounts)
     */
    public abstract boolean withdraw(double amount) throws InsufficientFundsException, OverdraftExceededException;

    /**
     * Displays the account details in a formatted manner.
     */
    public abstract void displayAccountDetails();

    /**
     * Returns the type of account (e.g., "Savings", "Checking").
     *
     * @return the account type as a string
     */
    public abstract String getAccountType();

    @Override
    public boolean processTransaction(double amount, String type) {
        try {
            if (type.equalsIgnoreCase("Deposit")) {
                deposit(amount);
                return true;
            } else if (type.equalsIgnoreCase("Withdrawal")) {
                return withdraw(amount);
            }
        } catch (InvalidAmountException | InsufficientFundsException | OverdraftExceededException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
        return false;
    }
}

