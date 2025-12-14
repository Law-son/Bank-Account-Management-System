package org.example.presentation;

import org.example.models.Account;
import org.example.models.Transaction;
import org.example.models.exceptions.AccountNotFoundException;
import org.example.models.exceptions.InsufficientFundsException;
import org.example.models.exceptions.InvalidAmountException;
import org.example.models.exceptions.OverdraftExceededException;
import org.example.services.AccountCreationService;
import org.example.services.AccountManager;
import org.example.services.StatementGenerator;
import org.example.services.TransactionManager;
import org.example.services.TransferService;
import org.example.utils.TestRunner;
import org.example.utils.ValidationUtils;

import java.util.Stack;

/**
 * Console-based presentation layer for the Bank Account Management System.
 * Handles all menu navigation and user interaction.
 * Follows Single Responsibility Principle by isolating UI concerns.
 */
public class ConsoleApp {
    private final AccountManager accountManager;
    private final TransactionManager transactionManager;
    private final StatementGenerator statementGenerator;
    private final TransferService transferService;
    private final AccountCreationService accountCreationService;
    private final Stack<Runnable> menuStack;
    
    /**
     * Constructs a ConsoleApp with required service dependencies.
     *
     * @param accountManager      the account manager service
     * @param transactionManager   the transaction manager service
     * @param statementGenerator   the statement generator service
     * @param transferService      the transfer service
     * @param accountCreationService the account creation service
     */
    public ConsoleApp(AccountManager accountManager, 
                     TransactionManager transactionManager,
                     StatementGenerator statementGenerator,
                     TransferService transferService,
                     AccountCreationService accountCreationService) {
        this.accountManager = accountManager;
        this.transactionManager = transactionManager;
        this.statementGenerator = statementGenerator;
        this.transferService = transferService;
        this.accountCreationService = accountCreationService;
        this.menuStack = new Stack<>();
    }
    
    /**
     * Starts the console application and begins menu navigation.
     */
    public void start() {
        seedData();
        menuStack.push(this::showMainMenu);
        
        while (!menuStack.isEmpty()) {
            menuStack.peek().run();
        }
        
        System.out.println("Thank you for using Bank Account Management System!");
        System.out.println("All data saved in memory. Remember to commit your latest changes to Git!");
        System.out.println("Goodbye!");
    }
    
    /**
     * Displays the main menu and handles user selection.
     */
    private void showMainMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║       BANK ACCOUNT MANAGEMENT - MAIN MENU        ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");
        
        System.out.println("1. Manage Accounts");
        System.out.println("2. Perform Transactions");
        System.out.println("3. Generate Account Statements");
        System.out.println("4. Save/Load Data");
        System.out.println("5. Run Concurrent Simulation");
        System.out.println("6. Exit\n");
        
        int choice = ValidationUtils.getIntInRange("Enter Choice", 1, 6);
        
        switch (choice) {
            case 1:
                menuStack.push(this::manageAccountsMenu);
                break;
            case 2:
                menuStack.push(this::processTransactionMenu);
                break;
            case 3:
                menuStack.push(this::viewHistoryMenu);
                break;
            case 4:
                menuStack.push(this::saveLoadDataMenu);
                break;
            case 5:
                menuStack.push(this::runConcurrentSimulationMenu);
                break;
            case 6:
                menuStack.pop();
                break;
        }
    }
    
    /**
     * Displays the account management menu.
     */
    private void manageAccountsMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║              MANAGE ACCOUNTS                      ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");
        
        System.out.println("1. Create Account");
        System.out.println("2. View Accounts");
        System.out.println("3. Go Back\n");
        
        int choice = ValidationUtils.getIntInRange("Enter Choice", 1, 3);
        
        switch (choice) {
            case 1:
                menuStack.push(this::createAccountMenu);
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
    
    /**
     * Displays the test menu and runs all JUnit tests.
     * Note: This method is kept for potential future use but is not currently in the main menu.
     */
    private void runTestsMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║                  RUN TESTS                        ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");
        
        // Run all JUnit tests
        TestRunner.runAllTests();
        
        ValidationUtils.getString("Press Enter to continue");
        menuStack.pop();
    }
    
    /**
     * Handles save/load data menu (placeholder for future implementation).
     */
    private void saveLoadDataMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║              SAVE/LOAD DATA                       ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");
        
        System.out.println("Save/Load Data functionality is coming soon!");
        System.out.println("This feature will allow you to:");
        System.out.println("  - Save account and transaction data to file");
        System.out.println("  - Load previously saved data from file");
        System.out.println("  - Export data in various formats\n");
        
        ValidationUtils.getString("Press Enter to continue");
        menuStack.pop();
    }
    
    /**
     * Handles concurrent simulation menu (placeholder for future implementation).
     */
    private void runConcurrentSimulationMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║         RUN CONCURRENT SIMULATION                 ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");
        
        System.out.println("Concurrent Simulation functionality is coming soon!");
        System.out.println("This feature will allow you to:");
        System.out.println("  - Simulate multiple concurrent transactions");
        System.out.println("  - Test thread safety of account operations");
        System.out.println("  - Analyze performance under concurrent load\n");
        
        ValidationUtils.getString("Press Enter to continue");
        menuStack.pop();
    }
    
    /**
     * Handles account creation flow.
     */
    private void createAccountMenu() {
        System.out.println("\nACCOUNT CREATION        ");
        System.out.println("--------------------------");
        System.out.println("Enter 0 to go back.\n");
        
        String name = ValidationUtils.getName("Enter customer name");
        if (name.equals("0")) {
            menuStack.pop();
            return;
        }
        
        int age = ValidationUtils.getIntPositive("Enter customer age");
        String contact = ValidationUtils.getContactNumber("Enter customer contact");
        String address = ValidationUtils.getString("Enter customer address");
        
        // Display customer type options
        System.out.println("\nCustomer type:                      ");
        String[] customerTypes = org.example.factories.CustomerFactory.getCustomerTypeDescriptions();
        for (int i = 0; i < customerTypes.length; i++) {
            System.out.println((i + 1) + ". " + customerTypes[i]);
        }
        System.out.println();
        int customerType = ValidationUtils.getIntInRange("Select type (1-" + customerTypes.length + ")", 1, customerTypes.length);
        
        // Display account type options
        System.out.println("\nAccount type:                      ");
        String[] accountTypes = org.example.factories.AccountFactory.getAccountTypeDescriptions();
        for (int i = 0; i < accountTypes.length; i++) {
            System.out.println((i + 1) + ". " + accountTypes[i]);
        }
        System.out.println();
        int accountType = ValidationUtils.getIntInRange("Select type (1-" + accountTypes.length + ")", 1, accountTypes.length);
        
        // Get initial deposit
        double initialDeposit;
        if (accountType == 1) { // Savings Account
            initialDeposit = ValidationUtils.getDoubleMin("Enter initial deposit amount",
                    org.example.models.SavingsAccount.getDefaultMinimumBalance());
        } else { // Checking Account
            initialDeposit = ValidationUtils.getDoublePositive("Enter initial deposit amount");
        }
        
        // Create and register account using service
        accountCreationService.createAndRegisterAccount(accountType, customerType, name, age, contact, address, initialDeposit);
        
        menuStack.pop();
    }
    
    /**
     * Handles transaction processing menu (Deposit, Withdrawal, Transfer).
     */
    private void processTransactionMenu() {
        System.out.println("\nPERFORM TRANSACTIONS    ");
        System.out.println("--------------------------");
        System.out.println("Enter 0 to go back.\n");
        
        String accountNumber = ValidationUtils.getString("Enter Account Number");
        if (accountNumber.equals("0")) {
            menuStack.pop();
            return;
        }
        
        Account account = findAccountOrShowError(accountNumber);
        if (account == null) {
            return;
        }
        
        displayAccountDetails(account);
        
        System.out.println("\nTransaction type: ");
        System.out.println("1. Deposit");
        System.out.println("2. Withdrawal");
        System.out.println("3. Transfer\n");
        int transactionType = ValidationUtils.getIntInRange("Select Type (1-3)", 1, 3);
        
        switch (transactionType) {
            case 1:
                handleDeposit(account);
                break;
            case 2:
                handleWithdrawal(account);
                break;
            case 3:
                handleTransfer(account);
                break;
        }
    }
    
    /**
     * Finds an account by account number or displays an error message.
     *
     * @param accountNumber the account number to search for
     * @return the account if found, null otherwise
     */
    private Account findAccountOrShowError(String accountNumber) {
        Account account = accountManager.findAccount(accountNumber);
        if (account == null) {
            displayError("Account not found. Please check the account number.");
            ValidationUtils.getString("Press Enter to continue");
            menuStack.pop();
        }
        return account;
    }
    
    /**
     * Displays account details.
     *
     * @param account the account to display
     */
    private void displayAccountDetails(Account account) {
        System.out.println("\nAccount details: ");
        System.out.printf("Customer: %s%n", account.getCustomer().getName());
        System.out.printf("Account Type: %s%n", account.getAccountType());
        System.out.printf("Current Balance: %s%n", ValidationUtils.formatAmount(account.getBalance()));
    }
    
    /**
     * Handles deposit transaction.
     *
     * @param account the account to deposit into
     */
    private void handleDeposit(Account account) {
        double amount = ValidationUtils.getDoublePositive("Enter Amount");
        
        double newBalance = account.getBalance() + amount;
        Transaction confirmationTxn = new Transaction(account.getAccountNumber(), "Deposit", amount, newBalance);
        confirmationTxn.displayTransactionDetails();
        
        String confirmation = ValidationUtils.getYesNo("Confirm transaction? (Y/N)");
        if (confirmation.equalsIgnoreCase("Y")) {
            boolean success = account.processTransaction(amount, "Deposit");
            if (success) {
                transactionManager.addTransaction(confirmationTxn);
                System.out.println("\nTransaction completed successfully!");
            } else {
                System.out.println("\nTransaction failed!");
            }
        }
        
        ValidationUtils.getString("Press Enter to continue");
        menuStack.pop();
    }
    
    /**
     * Handles withdrawal transaction.
     *
     * @param account the account to withdraw from
     */
    private void handleWithdrawal(Account account) {
        double amount = ValidationUtils.getDoublePositive("Enter Amount");
        
        try {
            // Validate withdrawal using account's own validation logic
            if (!account.canWithdraw(amount)) {
                throw new InsufficientFundsException(
                        "Transaction Failed: Insufficient funds. Current balance: " +
                                ValidationUtils.formatAmount(account.getBalance()));
            }
            
            double newBalance = account.getBalance() - amount;
            Transaction confirmationTxn = new Transaction(account.getAccountNumber(), "Withdrawal", amount, newBalance);
            confirmationTxn.displayTransactionDetails();
            
            String confirmation = ValidationUtils.getYesNo("Confirm transaction? (Y/N)");
            if (confirmation.equalsIgnoreCase("Y")) {
                boolean success = account.processTransaction(amount, "Withdrawal");
                if (success) {
                    transactionManager.addTransaction(confirmationTxn);
                    System.out.println("\nTransaction completed successfully!");
                } else {
                    System.out.println("\nTransaction failed!");
                }
            }
        } catch (InsufficientFundsException e) {
            displayError(e.getMessage());
        }
        
        ValidationUtils.getString("Press Enter to continue");
        menuStack.pop();
    }
    
    /**
     * Handles transfer transaction between two accounts.
     *
     * @param fromAccount the source account
     */
    private void handleTransfer(Account fromAccount) {
        String toAccountNumber = ValidationUtils.getString("Enter Destination Account Number");
        if (toAccountNumber.equals("0")) {
            menuStack.pop();
            return;
        }
        
        Account toAccount = findAccountOrShowError(toAccountNumber);
        if (toAccount == null) {
            return;
        }
        
        if (fromAccount.getAccountNumber().equals(toAccountNumber)) {
            displayError("Cannot transfer to the same account.");
            ValidationUtils.getString("Press Enter to continue");
            menuStack.pop();
            return;
        }
        
        double amount = ValidationUtils.getDoublePositive("Enter Amount");
        
        try {
            // Display transfer details
            System.out.println("\nTRANSFER DETAILS");
            System.out.println("---------------------------------");
            System.out.printf("From Account: %s (%s)%n", fromAccount.getAccountNumber(), fromAccount.getCustomer().getName());
            System.out.printf("To Account: %s (%s)%n", toAccount.getAccountNumber(), toAccount.getCustomer().getName());
            System.out.printf("Amount: %s%n", ValidationUtils.formatAmount(amount));
            System.out.printf("From Account Balance After: %s%n",
                    ValidationUtils.formatAmount(fromAccount.getBalance() - amount));
            System.out.printf("To Account Balance After: %s%n",
                    ValidationUtils.formatAmount(toAccount.getBalance() + amount));
            System.out.println("---------------------------------");
            
            String confirmation = ValidationUtils.getYesNo("Confirm transfer? (Y/N)");
            if (confirmation.equalsIgnoreCase("Y")) {
                transferService.transfer(fromAccount.getAccountNumber(), toAccountNumber, amount);
                System.out.println("\nTransfer completed successfully!");
            }
        } catch (AccountNotFoundException e) {
            displayError(e.getMessage());
        } catch (InvalidAmountException e) {
            displayError(e.getMessage());
        } catch (InsufficientFundsException e) {
            displayError(e.getMessage());
        } catch (OverdraftExceededException e) {
            displayError(e.getMessage());
        } catch (Exception e) {
            displayError("An unexpected error occurred: " + e.getMessage());
        }
        
        ValidationUtils.getString("Press Enter to continue");
        menuStack.pop();
    }
    
    /**
     * Handles account statement generation.
     */
    private void viewHistoryMenu() {
        System.out.println("\nGENERATE ACCOUNT STATEMENTS");
        System.out.println("---------------------------------");
        System.out.println("Enter 0 to go back\n");
        
        Account account = null;
        String accountNumber = "";
        
        while (account == null) {
            accountNumber = ValidationUtils.getString("Enter Account Number");
            if (accountNumber.equals("0")) {
                menuStack.pop();
                return;
            }
            
            account = accountManager.findAccount(accountNumber);
            if (account == null) {
                displayError("Account not found. Please check the account number.");
            }
        }
        
        statementGenerator.generateStatement(accountNumber, account);
        ValidationUtils.getString("Press Enter to continue");
        menuStack.pop();
    }
    
    /**
     * Displays an error message in a standardized format.
     *
     * @param message the error message to display
     */
    private void displayError(String message) {
        System.out.println("Error: " + message);
    }
    
    /**
     * Seeds initial data into the application for testing purposes.
     */
    private void seedData() {
        org.example.models.Customer c1 = new org.example.models.RegularCustomer("John Doe", 30, "+1-555-0101", "NY");
        org.example.models.Customer c2 = new org.example.models.PremiumCustomer("Jane Smith", 45, "+1-555-0202", "CA");
        
        Account acc1 = new org.example.models.SavingsAccount(c1, 1000);
        accountManager.addAccount(acc1, true);
        transactionManager.addTransaction(new Transaction(acc1.getAccountNumber(), "Deposit", 1000, 1000));
        
        Account acc2 = new org.example.models.SavingsAccount(c1, 2000);
        accountManager.addAccount(acc2, true);
        transactionManager.addTransaction(new Transaction(acc2.getAccountNumber(), "Deposit", 2000, 2000));
        
        Account acc3 = new org.example.models.SavingsAccount(c2, 5000);
        accountManager.addAccount(acc3, true);
        transactionManager.addTransaction(new Transaction(acc3.getAccountNumber(), "Deposit", 5000, 5000));
        
        Account acc4 = new org.example.models.CheckingAccount(c1, 1500);
        accountManager.addAccount(acc4, true);
        transactionManager.addTransaction(new Transaction(acc4.getAccountNumber(), "Deposit", 1500, 1500));
        
        Account acc5 = new org.example.models.CheckingAccount(c2, 12000);
        accountManager.addAccount(acc5, true);
        transactionManager.addTransaction(new Transaction(acc5.getAccountNumber(), "Deposit", 12000, 12000));
    }
}

