package org.example.services;

import org.example.factories.AccountFactory;
import org.example.factories.CustomerFactory;
import org.example.models.Account;
import org.example.models.Customer;
import org.example.models.Transaction;

/**
 * Service class responsible for account creation operations.
 * Follows Single Responsibility Principle by isolating account creation logic.
 */
public class AccountCreationService {
    private final AccountManager accountManager;
    private final TransactionManager transactionManager;
    
    /**
     * Constructs an AccountCreationService with required dependencies.
     *
     * @param accountManager      the account manager to add accounts
     * @param transactionManager   the transaction manager to record initial transactions
     */
    public AccountCreationService(AccountManager accountManager, TransactionManager transactionManager) {
        this.accountManager = accountManager;
        this.transactionManager = transactionManager;
    }
    
    /**
     * Creates a customer based on the provided information.
     *
     * @param customerType the type of customer (1 = Regular, 2 = Premium)
     * @param name         the customer's name
     * @param age          the customer's age
     * @param contact      the customer's contact number
     * @param address      the customer's address
     * @return the created customer
     */
    public Customer createCustomer(int customerType, String name, int age, String contact, String address) {
        return CustomerFactory.createCustomer(customerType, name, age, contact, address);
    }
    
    /**
     * Creates an account based on the provided information.
     *
     * @param accountType     the type of account (1 = Savings, 2 = Checking)
     * @param customer        the customer to associate with the account
     * @param initialDeposit  the initial deposit amount
     * @return the created account
     */
    public Account createAccount(int accountType, Customer customer, double initialDeposit) {
        return AccountFactory.createAccount(accountType, customer, initialDeposit);
    }
    
    /**
     * Creates and registers a new account with the system.
     * Also records the initial deposit transaction.
     *
     * @param accountType     the type of account (1 = Savings, 2 = Checking)
     * @param customerType    the type of customer (1 = Regular, 2 = Premium)
     * @param name            the customer's name
     * @param age             the customer's age
     * @param contact         the customer's contact number
     * @param address         the customer's address
     * @param initialDeposit  the initial deposit amount
     * @return the created and registered account
     */
    public Account createAndRegisterAccount(int accountType, int customerType, String name, int age, 
                                           String contact, String address, double initialDeposit) {
        // Create customer
        Customer customer = createCustomer(customerType, name, age, contact, address);
        
        // Create account
        Account account = createAccount(accountType, customer, initialDeposit);
        
        // Register account
        accountManager.addAccount(account);
        
        // Record initial deposit transaction
        Transaction initialDepositTransaction = new Transaction(
                account.getAccountNumber(),
                "Deposit",
                account.getBalance(),
                account.getBalance()
        );
        transactionManager.addTransaction(initialDepositTransaction);
        
        return account;
    }
}

