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
import org.example.services.AccountManager;
import org.example.services.StatementGenerator;
import org.example.services.TransactionManager;
import org.example.services.TransferService;
import org.example.utils.ValidationUtils;

import java.util.Stack;

/**
 * Main application class for the Bank Account Management System.
 * Handles menu navigation and user interactions.
 */
public class Main {
    private static final AccountManager accountManager = new AccountManager();
    private static final TransactionManager transactionManager = new TransactionManager();
    private static final StatementGenerator statementGenerator = new StatementGenerator(transactionManager);
    private static final TransferService transferService = new TransferService(accountManager, transactionManager);
    private static final Stack<Runnable> menuStack = new Stack<>();

    /**
     * Main entry point of the application.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        seedData();
        menuStack.push(Main::showMainMenu);

        while (!menuStack.isEmpty()) {
            menuStack.peek().run();
        }

        System.out.println("Thank you for using Bank Account Management System!");
        System.out.println("Goodbye!");
    }

    /**
     * Displays the main menu and handles user selection.
     */
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

    /**
     * Displays the account management menu.
     */
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

    /**
     * Displays the test menu (placeholder for future implementation).
     */
    private static void runTestsMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║                  RUN TESTS                        ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        System.out.println("Test functionality is coming soon!");
        System.out.println("This feature will be implemented in a future update.\n");

        ValidationUtils.getString("Press Enter to continue");
        menuStack.pop();
    }

    /**
     * Handles account creation flow.
     */
    private static void createAccountMenu() {
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

        Customer customer = createCustomer(name, age, contact, address);
        Account account = createAccount(customer);

        accountManager.addAccount(account);

        Transaction initialDepositTransaction = new Transaction(
                account.getAccountNumber(),
                "Deposit",
                account.getBalance(),
                account.getBalance()
        );
        transactionManager.addTransaction(initialDepositTransaction);

        menuStack.pop();
    }

    /**
     * Creates a customer based on user selection.
     *
     * @param name    customer name
     * @param age     customer age
     * @param contact customer contact number
     * @param address customer address
     * @return the created customer
     */
    private static Customer createCustomer(String name, int age, String contact, String address) {
        System.out.println("\nCustomer type:                      ");
        System.out.println("1. Regular Customer (Standard banking services)");
        System.out.println("2. Premium Customer (Enhanced benefits, min balance $10,000)\n");
        int customerType = ValidationUtils.getIntInRange("Select type (1-2)", 1, 2);

        if (customerType == 2) {
            return new PremiumCustomer(name, age, contact, address);
        } else {
            return new RegularCustomer(name, age, contact, address);
        }
    }

    /**
     * Creates an account based on user selection.
     *
     * @param customer the customer to associate with the account
     * @return the created account
     */
    private static Account createAccount(Customer customer) {
        System.out.println("\nAccount type:                      ");
        System.out.printf("1. Savings Account (Interest: %.1f%%, Min Balance: %s)%n",
                SavingsAccount.getDefaultInterestRate(),
                ValidationUtils.formatAmount(SavingsAccount.getDefaultMinimumBalance()));
        System.out.printf("2. Checking Account (Overdraft: %s, Monthly Fee: %s)%n%n",
                ValidationUtils.formatAmount(CheckingAccount.getDefaultOverdraftLimit()),
                ValidationUtils.formatAmount(CheckingAccount.getDefaultMonthlyFee()));
        int accType = ValidationUtils.getIntInRange("Select type (1-2)", 1, 2);

        if (accType == 1) {
            double initialDep = ValidationUtils.getDoubleMin("Enter initial deposit amount",
                    SavingsAccount.getDefaultMinimumBalance());
            return new SavingsAccount(customer, initialDep);
        } else {
            double initialDep = ValidationUtils.getDoublePositive("Enter initial deposit amount");
            return new CheckingAccount(customer, initialDep);
        }
    }

    /**
     * Handles transaction processing menu (Deposit, Withdrawal, Transfer).
     */
    private static void processTransactionMenu() {
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
    private static Account findAccountOrShowError(String accountNumber) {
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
    private static void displayAccountDetails(Account account) {
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
    private static void handleDeposit(Account account) {
        double amount = ValidationUtils.getDoublePositive("Enter Amount");

        try {
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
        } catch (Exception e) {
            displayError(e.getMessage());
        }

        ValidationUtils.getString("Press Enter to continue");
        menuStack.pop();
    }

    /**
     * Handles withdrawal transaction.
     *
     * @param account the account to withdraw from
     */
    private static void handleWithdrawal(Account account) {
        double amount = ValidationUtils.getDoublePositive("Enter Amount");

        try {
            // Validate withdrawal before showing confirmation
            validateWithdrawal(account, amount);

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
     * Validates if a withdrawal is possible without actually performing it.
     *
     * @param account the account to validate
     * @param amount  the amount to withdraw
     * @throws InsufficientFundsException if withdrawal is not possible
     */
    private static void validateWithdrawal(Account account, double amount) throws InsufficientFundsException {
        double currentBalance = account.getBalance();

        if (account instanceof SavingsAccount) {
            SavingsAccount savingsAccount = (SavingsAccount) account;
            if (currentBalance - amount < savingsAccount.getMinimumBalance()) {
                throw new InsufficientFundsException(
                        "Transaction Failed: Insufficient funds. Current balance: " +
                                ValidationUtils.formatAmount(currentBalance));
            }
        } else if (account instanceof CheckingAccount) {
            CheckingAccount checkingAccount = (CheckingAccount) account;
            if (currentBalance - amount < -checkingAccount.getOverdraftLimit()) {
                throw new InsufficientFundsException(
                        "Transaction Failed: Insufficient funds. Current balance: " +
                                ValidationUtils.formatAmount(currentBalance));
            }
        }
    }

    /**
     * Handles transfer transaction between two accounts.
     *
     * @param fromAccount the source account
     */
    private static void handleTransfer(Account fromAccount) {
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
        } catch (AccountNotFoundException | InvalidAmountException | InsufficientFundsException e) {
            displayError(e.getMessage());
        }

        ValidationUtils.getString("Press Enter to continue");
        menuStack.pop();
    }

    /**
     * Handles account statement generation.
     */
    private static void viewHistoryMenu() {
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
    private static void displayError(String message) {
        System.out.println("Error: " + message);
    }

    /**
     * Seeds initial data into the application for testing purposes.
     */
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
