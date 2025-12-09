package org.example;

import org.example.presentation.ConsoleApp;
import org.example.services.AccountCreationService;
import org.example.services.AccountManager;
import org.example.services.StatementGenerator;
import org.example.services.TransactionManager;
import org.example.services.TransferService;

/**
 * Main application class for the Bank Account Management System.
 * Handles application startup and delegates to ConsoleApp for user interaction.
 * Follows Single Responsibility Principle by only handling application entry point.
 */
public class Main {
    /**
     * Main entry point of the application.
     * Initializes services and starts the console application.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        // Initialize services
        AccountManager accountManager = new AccountManager();
        TransactionManager transactionManager = new TransactionManager();
        StatementGenerator statementGenerator = new StatementGenerator(transactionManager);
        TransferService transferService = new TransferService(accountManager, transactionManager);
        AccountCreationService accountCreationService = new AccountCreationService(accountManager, transactionManager);
        
        // Create and start console application
        ConsoleApp app = new ConsoleApp(
                accountManager,
                transactionManager,
                statementGenerator,
                transferService,
                accountCreationService
        );
        
        app.start();
    }
}
