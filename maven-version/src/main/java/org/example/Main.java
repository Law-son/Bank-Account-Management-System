package org.example;


import org.example.models.Account;
import org.example.models.CheckingAccount;
import org.example.models.Customer;
import org.example.models.PremiumCustomer;
import org.example.models.RegularCustomer;
import org.example.models.SavingsAccount;
import org.example.models.Transaction;
import org.example.models.exceptions.AccountNotFoundException;
import org.example.models.exceptions.InsufficientFundsException;
import org.example.models.exceptions.InvalidAmountException;
import org.example.models.exceptions.MinimumBalanceException;
import org.example.models.exceptions.OverdraftExceededException;
import org.example.services.AccountManager;
import org.example.services.StatementGenerator;
import org.example.services.TransactionManager;
import org.example.utils.ValidationUtils;

import java.util.Stack;

public class Main {
    private static AccountManager accountManager = new AccountManager();
    private static TransactionManager transactionManager = new TransactionManager();
    private static StatementGenerator statementGenerator = new StatementGenerator(transactionManager);

    //  Stack for handling navigation
    private static Stack<Runnable> menuStack = new Stack<>();

    public static void main(String[] args) {
        seedData();

        // Push the Main Menu as the first item
        menuStack.push(Main::showMainMenu);

        // Navigation Loop
        while (!menuStack.isEmpty()) {
            menuStack.peek().run();
        }

        System.out.println("Thank you for using Bank Account Management System!");
        System.out.println("Goodbye!");
    }

    // Menu
    private static void showMainMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║       BANK ACCOUNT MANAGEMENT - MAIN MENU        ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        System.out.println("1. Manage Accounts");
        System.out.println("2. Perform Transactions");
        System.out.println("3. Generate Account Statements");
        System.out.println("4. Run Tests");
        System.out.println("5. Exit\n");

        int choice = ValidationUtils.getIntInRange("Enter Choice", 1, 5);

        switch (choice) {
            case 1:
                menuStack.push(Main::manageAccountsMenu);
                break;
            case 2:
                menuStack.push(Main::processTransactionMenu);
                break;
            case 3:
                menuStack.push(Main::viewHistoryMenu);
                break;
            case 4:
                menuStack.push(Main::runTestsMenu);
                break;
            case 5:
                menuStack.pop();
                break;
        }
    }

    private static void manageAccountsMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║              MANAGE ACCOUNTS                      ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        System.out.println("1. Create Account");
        System.out.println("2. View Accounts");
        System.out.println("3. Go Back\n");

        int choice = ValidationUtils.getIntInRange("Enter Choice", 1, 3);

        switch (choice) {
            case 1:
                menuStack.push(Main::createAccountMenu);
                break;
            case 2:
                accountManager.viewAllAccounts();
                ValidationUtils.getString("Press Enter to continue");
                menuStack.pop();
                break;
            case 3:
                menuStack.pop();
                break;
        }
    }

    private static void runTestsMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║                  RUN TESTS                        ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        System.out.println("Test functionality is coming soon!");
        System.out.println("This feature will be implemented in a future update.\n");

        ValidationUtils.getString("Press Enter to continue");
        menuStack.pop();
    }

    private static void createAccountMenu() {
        System.out.println("\nACCOUNT CREATION        ");
        System.out.println("--------------------------");
        System.out.println("Enter 0 to go back.\n");

        String name = ValidationUtils.getName("Enter customer name");
        if(name.equals("0")) { menuStack.pop(); return; }

        int age = ValidationUtils.getIntPositive("Enter customer age");
        String contact = ValidationUtils.getContactNumber("Enter customer contact");
        String address = ValidationUtils.getString("Enter customer address");

        // Select Customer Type
        System.out.println("\nCustomer type:                      ");
        System.out.println("1. Regular Customer (Standard banking services)");
        System.out.println("2. Premium Customer (Enhanced benefits, min balance $10,000)\n");
        int customerType = ValidationUtils.getIntInRange("Select type (1-2)", 1, 2);

        Customer customer;
        if (customerType == 2) {
            customer = new PremiumCustomer(name, age, contact, address);
        } else {
            customer = new RegularCustomer(name, age, contact, address);
        }

        System.out.println("\nAccount type:                      ");
        System.out.printf("1. Savings Account (Interest: %.1f%%, Min Balance: %s)%n", 
                SavingsAccount.getDefaultInterestRate(), ValidationUtils.formatAmount(SavingsAccount.getDefaultMinimumBalance()));
        System.out.printf("2. Checking Account (Overdrift: %s, Monthly Fee: %s)%n%n", 
                ValidationUtils.formatAmount(CheckingAccount.getDefaultOverdraftLimit()),
                ValidationUtils.formatAmount(CheckingAccount.getDefaultMonthlyFee()));
        int accType = ValidationUtils.getIntInRange("Select type (1-2)", 1, 2);
        
        Account account;
        if (accType == 1) {
            double initialDep = ValidationUtils.getDoubleMin("Enter initial deposit amount", SavingsAccount.getDefaultMinimumBalance());
            account = new SavingsAccount(customer, initialDep);
        } else {
            double initialDep = ValidationUtils.getDoublePositive("Enter initial deposit amount");
            account = new CheckingAccount(customer, initialDep);
        }

        accountManager.addAccount(account);
        
        // Create and store transaction for initial deposit
        Transaction initialDepositTransaction = new Transaction(
            account.getAccountNumber(), 
            "Deposit", 
            account.getBalance(), 
            account.getBalance()
        );
        transactionManager.addTransaction(initialDepositTransaction);
        
        menuStack.pop(); // Return to previous menu after success
    }

    private static void processTransactionMenu() {
        System.out.println("\nPERFORM TRANSACTIONS    ");
        System.out.println("--------------------------");
        System.out.println("Enter 0 to go back.\n");

        String accNum = ValidationUtils.getString("Enter Account Number");
        if (accNum.equals("0")) { menuStack.pop(); return; }

        Account account = accountManager.findAccount(accNum);
        if (account == null) {
            try {
                throw new AccountNotFoundException("Account not found.");
            } catch (AccountNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            }
            ValidationUtils.getString("Press Enter to continue");
            menuStack.pop();
            return;
        }

        // Display account details
        System.out.println("\nAccount details: ");
        System.out.printf("Customer: %s%n", account.getCustomer().getName());
        System.out.printf("Account Type: %s%n", account.getAccountType());
        System.out.printf("Current Balance: %s%n", ValidationUtils.formatAmount(account.getBalance()));

        // Select transaction type
        System.out.println("\nTransaction type: ");
        System.out.println("1. Deposit");
        System.out.println("2. Withdrawal\n");
        int type = ValidationUtils.getIntInRange("Select Type (1-2)", 1, 2);

        // Enter amount
        double amount = ValidationUtils.getDoublePositive("Enter Amount");

        String transactionType = (type == 1) ? "Deposit" : "Withdrawal";
        double previousBalance = account.getBalance();
        double newBalance;

        // Validate withdrawal before showing confirmation (without processing)
        if (type == 2) {
            try {
                if (account instanceof SavingsAccount) {
                    SavingsAccount savingsAccount = (SavingsAccount) account;
                    if (previousBalance - amount < savingsAccount.getMinimumBalance()) {
                        throw new MinimumBalanceException("Transaction Failed: Minimum balance of " + ValidationUtils.formatAmount(savingsAccount.getMinimumBalance()) + " must be maintained.");
                    }
                } else if (account instanceof CheckingAccount) {
                    CheckingAccount checkingAccount = (CheckingAccount) account;
                    if (previousBalance - amount < -checkingAccount.getOverdraftLimit()) {
                        throw new OverdraftExceededException("Transaction Failed: Exceeds overdraft limit of " + ValidationUtils.formatAmount(checkingAccount.getOverdraftLimit()));
                    }
                }
                newBalance = previousBalance - amount;
            } catch (MinimumBalanceException | OverdraftExceededException e) {
                System.out.println("Error: " + e.getMessage());
                ValidationUtils.getString("Press Enter to continue");
                menuStack.pop();
                return;
            }
        } else {
            newBalance = previousBalance + amount;
        }

        // Create transaction object for confirmation display
        Transaction confirmationTxn = new Transaction(accNum, transactionType, amount, newBalance);
        confirmationTxn.displayTransactionDetails();

        String confirmation = ValidationUtils.getYesNo("Confirm transaction? (Y/N)");
        
        if (confirmation.equalsIgnoreCase("Y")) {
            // Use processTransaction method from Transactable interface
            boolean success = account.processTransaction(amount, transactionType);
            
            if (success) {
                // Add transaction details (reuse the same transaction object)
                transactionManager.addTransaction(confirmationTxn);
                
                System.out.println("\nTransaction completed successfully!");
            } else {
                System.out.println("\nTransaction failed!");
            }
            
            ValidationUtils.getString("Press Enter to continue");
            menuStack.pop();
        } else {
            ValidationUtils.getString("Press Enter to continue");
            menuStack.pop();
        }
    }

    private static void viewHistoryMenu() {
        System.out.println("\nGENERATE ACCOUNT STATEMENTS");
        System.out.println("---------------------------------");
        System.out.println("Enter 0 to go back\n");
        
        Account account = null;
        String accNum = "";
        
        // Loop until valid account is found or user enters 0 to go back
        while (account == null) {
            accNum = ValidationUtils.getString("Enter Account Number");
            if (accNum.equals("0")) { 
                menuStack.pop(); 
                return; 
            }
            
            account = accountManager.findAccount(accNum);
            if (account == null) {
                try {
                    throw new AccountNotFoundException("Account not found.");
                } catch (AccountNotFoundException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
        
        statementGenerator.generateStatement(accNum, account);
        ValidationUtils.getString("Press Enter to continue");
        menuStack.pop();
    }

    // Method to seed initial data into the application
    private static void seedData() {
        Customer c1 = new RegularCustomer("John Doe", 30, "+1-555-0101", "NY");
        Customer c2 = new PremiumCustomer("Jane Smith", 45, "+1-555-0202", "CA");

        Account acc1 = new SavingsAccount(c1, 1000);
        accountManager.addAccount(acc1, true);
        transactionManager.addTransaction(new Transaction(acc1.getAccountNumber(), "Deposit", 1000, 1000));

        Account acc2 = new SavingsAccount(c1, 2000);
        accountManager.addAccount(acc2, true);
        transactionManager.addTransaction(new Transaction(acc2.getAccountNumber(), "Deposit", 2000, 2000));

        Account acc3 = new SavingsAccount(c2, 5000);
        accountManager.addAccount(acc3, true);
        transactionManager.addTransaction(new Transaction(acc3.getAccountNumber(), "Deposit", 5000, 5000));

        Account acc4 = new CheckingAccount(c1, 1500);
        accountManager.addAccount(acc4, true);
        transactionManager.addTransaction(new Transaction(acc4.getAccountNumber(), "Deposit", 1500, 1500));

        Account acc5 = new CheckingAccount(c2, 12000);
        accountManager.addAccount(acc5, true);
        transactionManager.addTransaction(new Transaction(acc5.getAccountNumber(), "Deposit", 12000, 12000));
    }
}