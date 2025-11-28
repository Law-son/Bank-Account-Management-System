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
        // Seed some data for testing [cite: 8]
        seedData();

        // Push the Main Menu as the first item
        menuStack.push(Main::showMainMenu);

        // Navigation Loop
        while (!menuStack.isEmpty()) {
            // Execute the current menu
            menuStack.peek().run();
        }

        System.out.println("Exiting Application. Goodbye!");
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

        int choice = InputValidator.getInt("Enter Choice");

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
            default:
                System.out.println("Invalid option.");
        }
    }

    private static void createAccountMenu() {
        System.out.println("\nACCOUNT CREATION        ");
        System.out.println("--------------------------");
        System.out.println("Enter 0 to go back.\n");

        String name = InputValidator.getString("Enter customer name");
        if(name.equals("0")) { menuStack.pop(); return; }

        int age = InputValidator.getInt("Enter customer age");
        String contact = InputValidator.getString("Enter customer contact");
        String address = InputValidator.getString("Enter customer address");

        // Select Customer Type
        System.out.println("\nCustomer type:                      ");
        System.out.println("1. Regular Customer (Standard banking services)");
        System.out.println("2. Premium Customer (Enhanced benefits, min balance $10,000)\n");
        int customerType = InputValidator.getInt("Select type (1-2)");

        // TODO: enforce validation here for other inputs aside 1 and 2
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
        int accType = InputValidator.getInt("Select type (1-2)");
        double initialDep = InputValidator.getDouble("Enter initial deposit amount");

        Account account;
        if (accType == 1) {
            if (initialDep < 500) { System.out.println("Error: Min deposit for Savings is $500"); return; }
            account = new SavingsAccount(customer, initialDep);
        } else {
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
            return;
        } else {
            System.out.print("\nAccount details:%n");
            System.out.printf("Customer: %s%n", account.getCustomer().getName());
            System.out.printf("Account Type: %s%n", account.getAccountType());
            System.out.printf("Current Balance: %2f%n", account.getBalance());
        }

        System.out.println("\nTransaction type: ");
        System.out.println("1. Deposit");
        System.out.println("2. Withdrawal\n");
        int type = InputValidator.getInt("Select Type (1-2)");
        double amount = InputValidator.getDouble("Enter Amount");

        boolean success = false;
        String transactionType = "";

        if (type == 1) {
            transactionType = "Deposit";
            account.deposit(amount);
            success = true;
        } else if (type == 2) {
            transactionType = "Withdrawal";
            success = account.withdraw(amount);
        }

        if (success) {
            Transaction txn = new Transaction(accNum, transactionType, amount, account.getBalance());
            transactionManager.addTransaction(txn);
            System.out.println("Transaction Successful. New Balance: $" + account.getBalance());
        }

        // Don't pop automatically, let them do another txn or type 0 to back
    }

    private static void viewHistoryMenu() {
        System.out.println("\n--- View Transaction History ---");
        String accNum = InputValidator.getString("Enter Account Number (or '0' to back)");
        if (accNum.equals("0")) { menuStack.pop(); return; }

        transactionManager.viewTransactionsByAccount(accNum);
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