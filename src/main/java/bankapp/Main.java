package main.java.bankapp;

import main.java.bankapp.managers.AccountManager;
import main.java.bankapp.models.accounts.Account;
import main.java.bankapp.models.accounts.CheckingAccount;
import main.java.bankapp.models.accounts.SavingsAccount;
import main.java.bankapp.models.customers.Customer;
import main.java.bankapp.models.customers.PremiumCustomer;
import main.java.bankapp.models.customers.RegularCustomer;
import main.java.bankapp.models.transactions.Transactable;
import main.java.bankapp.models.transactions.Transaction;
import main.java.bankapp.models.transactions.TransactionManager;
import main.java.bankapp.utils.InputValidator;

import java.util.Stack;

public class Main implements Transactable {
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
        System.out.println("╔══════════════════════════════════════════════════╗");
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

        String name = InputValidator.getString("Enter customer name");
        if(name.equals("0")) { menuStack.pop(); return; }

        int age = InputValidator.getIntPositive("Enter customer age");
        String contact = InputValidator.getString("Enter customer contact");
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

        // Select Account Type
        System.out.println("\nAccount type:                      ");
        System.out.println("1. Savings Account (Interest: 3.5%, Min Balance: $500)");
        System.out.println("2. Checking Account (Overdrift: $1,000, Monthly Fee: $10)\n");
        int accType = InputValidator.getIntInRange("Select type (1-2)", 1, 2);
        
        Account account;
        if (accType == 1) {
            double initialDep = InputValidator.getDoubleMin("Enter initial deposit amount", 500.0);
            account = new SavingsAccount(customer, initialDep);
        } else {
            double initialDep = InputValidator.getDoublePositive("Enter initial deposit amount");
            account = new CheckingAccount(customer, initialDep);
        }

        accountManager.addAccount(account);
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
            InputValidator.getString("Press Enter to continue...");
            menuStack.pop();
            return;
        }

        // Display account details
        System.out.println("\nAccount details: ");
        System.out.printf("Customer: %s%n", account.getCustomer().getName());
        System.out.printf("Account Type: %s%n", account.getAccountType());
        System.out.printf("Current Balance: $%.2f%n", account.getBalance());

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
                // For SavingsAccount: balance - amount >= minimumBalance (500)
                canWithdraw = (previousBalance - amount >= 500);
            } else if (account instanceof CheckingAccount) {
                // For CheckingAccount: balance - amount >= -overdraftLimit (-1000)
                canWithdraw = (previousBalance - amount >= -1000);
            }
            
            if (!canWithdraw) {
                if (account instanceof SavingsAccount) {
                    System.out.println("Transaction Failed: Minimum balance of $500.00 must be maintained.");
                } else {
                    System.out.println("Transaction Failed: Exceeds overdraft limit of $1000.00");
                }
                InputValidator.getString("Press Enter to continue");
                menuStack.pop();
                return;
            }
            newBalance = previousBalance - amount;
        } else {
            newBalance = previousBalance + amount;
        }

        // Show transaction confirmation details
        System.out.println("\nTRANSACTION CONFIRMATION");
        System.out.println("---------------------------------");
        System.out.printf("Account: %s%n", accNum);
        System.out.printf("Type: %s%n", transactionType);
        System.out.printf("Amount: $%.2f%n", amount);
        System.out.printf("Previous Balance: $%.2f%n", previousBalance);
        System.out.printf("New Balance: $%.2f%n", newBalance);
        System.out.println("---------------------------------");

        // Ask for confirmation
        String confirmation = InputValidator.getYesNo("Confirm transaction? (Y/N)");
        
        if (confirmation.equalsIgnoreCase("Y")) {
            // Process the transaction
            if (type == 1) {
                account.deposit(amount);
            } else {
                // Process withdrawal (validation already done, so this should succeed)
                account.withdraw(amount);
            }
            
            // Add transaction details
            Transaction txn = new Transaction(accNum, transactionType, amount, account.getBalance());
            transactionManager.addTransaction(txn);
            
            // Show transaction successful message
            System.out.println("\nTransaction completed successfully!");
            
            // Press Enter to continue
            InputValidator.getString("Press Enter to continue");
            menuStack.pop();
        } else {
            // User didn't confirm
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
        
        // Account found, proceed with viewing transactions
        transactionManager.viewTransactionsByAccount(accNum, account);
        InputValidator.getString("Press Enter to continue");
        menuStack.pop();
    }

    @Override
    public boolean processTransaction(double amount, String type) {
        return false;
    }

    private static void seedData() {
        Customer c1 = new RegularCustomer("John Doe", 30, "555-0101", "NY");
        Customer c2 = new PremiumCustomer("Jane Smith", 45, "555-0202", "CA");

        accountManager.addAccount(new SavingsAccount(c1, 1000));
        accountManager.addAccount(new SavingsAccount(c1, 2000));
        accountManager.addAccount(new SavingsAccount(c2, 5000));

        accountManager.addAccount(new CheckingAccount(c1, 1500));
        accountManager.addAccount(new CheckingAccount(c2, 12000));
    }
}