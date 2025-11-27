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

    // --- Menus ---

    private static void showMainMenu() {
        System.out.println("\n=== BANK ACCOUNT MANAGEMENT SYSTEM ===");
        System.out.println("1. Create Account");
        System.out.println("2. View Accounts");
        System.out.println("3. Process Transaction");
        System.out.println("4. View Transaction History");
        System.out.println("5. Exit");

        int choice = InputValidator.getInt("Select Option");

        switch (choice) {
            case 1:
                menuStack.push(Main::createAccountMenu); // Push new screen
                break;
            case 2:
                accountManager.viewAllAccounts();
                // We stay on this screen in the stack, or just pause
                InputValidator.getString("Press Enter to continue");
                break;
            case 3:
                menuStack.push(Main::processTransactionMenu);
                break;
            case 4:
                menuStack.push(Main::viewHistoryMenu);
                break;
            case 5:
                menuStack.pop(); // Remove main menu -> Stack empty -> Loop ends
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private static void createAccountMenu() {
        System.out.println("\n--- Create New Account ---");
        System.out.println("0. Back");

        String name = InputValidator.getString("Customer Name");
        if(name.equals("0")) { menuStack.pop(); return; }

        int age = InputValidator.getInt("Age");
        String contact = InputValidator.getString("Contact");
        String address = InputValidator.getString("Address");

        // Select Customer Type
        System.out.println("Customer Type: 1. Regular 2. Premium");
        int custType = InputValidator.getInt("Choice");

        Customer customer;
        if (custType == 2) {
            customer = new PremiumCustomer(name, age, contact, address);
        } else {
            customer = new RegularCustomer(name, age, contact, address);
        }

        // Select Account Type
        System.out.println("Account Type: 1. Savings 2. Checking");
        int accType = InputValidator.getInt("Choice");
        double initialDep = InputValidator.getDouble("Initial Deposit");

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
        System.out.println("\n--- Process Transaction ---");
        String accNum = InputValidator.getString("Enter Account Number (or '0' to back)");
        if (accNum.equals("0")) { menuStack.pop(); return; }

        Account account = accountManager.findAccount(accNum);
        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        System.out.println("1. Deposit");
        System.out.println("2. Withdraw");
        int type = InputValidator.getInt("Select Type");
        double amount = InputValidator.getDouble("Enter Amount");

        boolean success = false;
        String typeStr = "";

        if (type == 1) {
            typeStr = "Deposit";
            account.deposit(amount);
            success = true;
        } else if (type == 2) {
            typeStr = "Withdrawal";
            success = account.withdraw(amount);
        }

        if (success) {
            Transaction txn = new Transaction(accNum, typeStr, amount, account.getBalance());
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

    // Required by Interface Transactable (Main implements it to fulfill req, 
    // though logic is delegated to Account classes usually)
    @Override
    public boolean processTransaction(double amount, String type) {
        return false;
    }

    private static void seedData() {
        // [cite: 8] Create 3 Savings, 2 Checking
        Customer c1 = new RegularCustomer("John Doe", 30, "555-0101", "NY");
        Customer c2 = new PremiumCustomer("Jane Smith", 45, "555-0202", "CA");

        accountManager.addAccount(new SavingsAccount(c1, 1000));
        accountManager.addAccount(new SavingsAccount(c1, 2000));
        accountManager.addAccount(new SavingsAccount(c2, 5000));

        accountManager.addAccount(new CheckingAccount(c1, 1500));
        accountManager.addAccount(new CheckingAccount(c2, 12000));
    }
}