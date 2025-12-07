package org.example;


import org.example.models.accounts.Account;
import org.example.models.accounts.AccountManager;
import org.example.models.accounts.CheckingAccount;
import org.example.models.accounts.SavingsAccount;
import org.example.models.customers.Customer;
import org.example.models.customers.PremiumCustomer;
import org.example.models.customers.RegularCustomer;
import org.example.models.transactions.Transaction;
import org.example.models.transactions.TransactionManager;
import org.example.utils.InputValidator;

import java.util.Stack;

public class Main {
    private static AccountManager accountManager = new AccountManager();
    private static TransactionManager transactionManager = new TransactionManager();

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

        System.out.println("1. Create Account");
        System.out.println("2. View Accounts");
        System.out.println("3. Process Transaction");
        System.out.println("4. View Transaction History");
        System.out.println("5. Exit\n");

        int choice = InputValidator.getIntInRange("Enter Choice", 1, 5);

        switch (choice) {
            case 1:
                menuStack.push(Main::createAccountMenu);
                break;
            case 2:
                accountManager.viewAllAccounts();
                InputValidator.getString("Press Enter to continue");
                break;
            case 3:
                menuStack.push(Main::processTransactionMenu);
                break;
            case 4:
                menuStack.push(Main::viewHistoryMenu);
                break;
            case 5:
                menuStack.pop();
                break;
        }
    }

    private static void createAccountMenu() {
        System.out.println("\nACCOUNT CREATION        ");
        System.out.println("--------------------------");
        System.out.println("Enter 0 to go back.\n");

        String name = InputValidator.getName("Enter customer name");
        if(name.equals("0")) { menuStack.pop(); return; }

        int age = InputValidator.getIntPositive("Enter customer age");
        String contact = InputValidator.getContactNumber("Enter customer contact");
        String address = InputValidator.getString("Enter customer address");

        // Select Customer Type
        System.out.println("\nCustomer type:                      ");
        System.out.println("1. Regular Customer (Standard banking services)");
        System.out.println("2. Premium Customer (Enhanced benefits, min balance $10,000)\n");
        int customerType = InputValidator.getIntInRange("Select type (1-2)", 1, 2);

        Customer customer;
        if (customerType == 2) {
            customer = new PremiumCustomer(name, age, contact, address);
        } else {
            customer = new RegularCustomer(name, age, contact, address);
        }

        System.out.println("\nAccount type:                      ");
        System.out.printf("1. Savings Account (Interest: %.1f%%, Min Balance: %s)%n", 
                SavingsAccount.getDefaultInterestRate(), InputValidator.formatAmount(SavingsAccount.getDefaultMinimumBalance()));
        System.out.printf("2. Checking Account (Overdrift: %s, Monthly Fee: %s)%n%n", 
                InputValidator.formatAmount(CheckingAccount.getDefaultOverdraftLimit()),
                InputValidator.formatAmount(CheckingAccount.getDefaultMonthlyFee()));
        int accType = InputValidator.getIntInRange("Select type (1-2)", 1, 2);
        
        Account account;
        if (accType == 1) {
            double initialDep = InputValidator.getDoubleMin("Enter initial deposit amount", SavingsAccount.getDefaultMinimumBalance());
            account = new SavingsAccount(customer, initialDep);
        } else {
            double initialDep = InputValidator.getDoublePositive("Enter initial deposit amount");
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
        System.out.println("\nPROCESS TRANSACTION     ");
        System.out.println("--------------------------");
        System.out.println("Enter 0 to go back.\n");

        String accNum = InputValidator.getString("Enter Account Number");
        if (accNum.equals("0")) { menuStack.pop(); return; }

        Account account = accountManager.findAccount(accNum);
        if (account == null) {
            System.out.println("Account not found.");
            InputValidator.getString("Press Enter to continue");
            menuStack.pop();
            return;
        }

        // Display account details
        System.out.println("\nAccount details: ");
        System.out.printf("Customer: %s%n", account.getCustomer().getName());
        System.out.printf("Account Type: %s%n", account.getAccountType());
        System.out.printf("Current Balance: %s%n", InputValidator.formatAmount(account.getBalance()));

        // Select transaction type
        System.out.println("\nTransaction type: ");
        System.out.println("1. Deposit");
        System.out.println("2. Withdrawal\n");
        int type = InputValidator.getIntInRange("Select Type (1-2)", 1, 2);

        // Enter amount
        double amount = InputValidator.getDoublePositive("Enter Amount");

        String transactionType = (type == 1) ? "Deposit" : "Withdrawal";
        double previousBalance = account.getBalance();
        double newBalance;

        // Validate withdrawal before showing confirmation (without processing)
        if (type == 2) {
            boolean canWithdraw = false;
            if (account instanceof SavingsAccount) {
                SavingsAccount savingsAccount = (SavingsAccount) account;
                canWithdraw = (previousBalance - amount >= savingsAccount.getMinimumBalance());
            } else if (account instanceof CheckingAccount) {
                CheckingAccount checkingAccount = (CheckingAccount) account;
                canWithdraw = (previousBalance - amount >= -checkingAccount.getOverdraftLimit());
            }
            
            if (!canWithdraw) {
                if (account instanceof SavingsAccount) {
                    SavingsAccount savingsAccount = (SavingsAccount) account;
                    System.out.println("Transaction Failed: Minimum balance of " + InputValidator.formatAmount(savingsAccount.getMinimumBalance()) + " must be maintained.");
                } else {
                    CheckingAccount checkingAccount = (CheckingAccount) account;
                    System.out.println("Transaction Failed: Exceeds overdraft limit of " + InputValidator.formatAmount(checkingAccount.getOverdraftLimit()));
                }
                InputValidator.getString("Press Enter to continue");
                menuStack.pop();
                return;
            }
            newBalance = previousBalance - amount;
        } else {
            newBalance = previousBalance + amount;
        }

        // Create transaction object for confirmation display
        Transaction confirmationTxn = new Transaction(accNum, transactionType, amount, newBalance);
        confirmationTxn.displayTransactionDetails();

        String confirmation = InputValidator.getYesNo("Confirm transaction? (Y/N)");
        
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
            
            InputValidator.getString("Press Enter to continue");
            menuStack.pop();
        } else {
            InputValidator.getString("Press Enter to continue");
            menuStack.pop();
        }
    }

    private static void viewHistoryMenu() {
        System.out.println("\nVIEW TRANSACTION HISTORY");
        System.out.println("---------------------------------");
        System.out.println("Enter 0 to go back\n");
        
        Account account = null;
        String accNum = "";
        
        // Loop until valid account is found or user enters 0 to go back
        while (account == null) {
            accNum = InputValidator.getString("Enter Account Number");
            if (accNum.equals("0")) { 
                menuStack.pop(); 
                return; 
            }
            
            account = accountManager.findAccount(accNum);
            if (account == null) {
                System.out.println("Account not found.");
            }
        }
        
        transactionManager.viewTransactionsByAccount(accNum, account);
        InputValidator.getString("Press Enter to continue");
        menuStack.pop();
    }

    // Method to seed initial data into the application
    private static void seedData() {
        Customer c1 = new RegularCustomer("John Doe", 30, "555-0101", "NY");
        Customer c2 = new PremiumCustomer("Jane Smith", 45, "555-0202", "CA");

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