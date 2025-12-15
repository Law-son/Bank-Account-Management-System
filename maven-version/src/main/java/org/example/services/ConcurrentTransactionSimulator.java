package org.example.services;

import org.example.models.Account;
import org.example.models.Transaction;
import org.example.models.exceptions.InsufficientFundsException;
import org.example.models.exceptions.InvalidAmountException;
import org.example.models.exceptions.OverdraftExceededException;
import org.example.utils.ValidationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Service class for simulating concurrent transactions on accounts.
 * Demonstrates thread-safe operations using synchronized methods and parallel streams.
 */
public class ConcurrentTransactionSimulator {
    private final AccountManager accountManager;
    private final TransactionManager transactionManager;
    
    /**
     * Represents a transaction operation to be performed.
     */
    public static class TransactionOperation {
        private final String type; // "Deposit" or "Withdrawal"
        private final double amount;
        private final int threadNumber;
        
        public TransactionOperation(String type, double amount, int threadNumber) {
            this.type = type;
            this.amount = amount;
            this.threadNumber = threadNumber;
        }
        
        public String getType() { return type; }
        public double getAmount() { return amount; }
        public int getThreadNumber() { return threadNumber; }
    }
    
    public ConcurrentTransactionSimulator(AccountManager accountManager, TransactionManager transactionManager) {
        this.accountManager = accountManager;
        this.transactionManager = transactionManager;
    }
    
    /**
     * Simulates concurrent transactions on a single account using threads.
     * Uses ExecutorService for managing thread execution.
     *
     * @param accountNumber the account number to perform transactions on
     * @param operations list of transaction operations to execute concurrently
     * @return the final balance after all transactions
     * @throws InterruptedException if thread execution is interrupted
     */
    public double simulateConcurrentTransactions(String accountNumber, List<TransactionOperation> operations) 
            throws InterruptedException {
        Account account = accountManager.findAccount(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        
        double initialBalance = account.getBalance();
        
        // Create ExecutorService with custom thread factory for proper naming
        ThreadFactory threadFactory = new ThreadFactory() {
            private int threadNumber = 1;
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "Thread-" + threadNumber++);
                return thread;
            }
        };
        ExecutorService executor = Executors.newFixedThreadPool(operations.size(), threadFactory);
        List<Future<Boolean>> futures = new ArrayList<>();
        
        // Submit all transaction operations to thread pool
        for (TransactionOperation operation : operations) {
            Future<Boolean> future = executor.submit(() -> {
                try {
                    String threadName = Thread.currentThread().getName();
                    String action = "Depositing".equals(operation.getType()) ? "Depositing" : 
                                   "Withdrawing".equals(operation.getType()) ? "Withdrawing" : 
                                   operation.getType();
                    System.out.println(threadName + ": " + action +
                                     " " + ValidationUtils.formatAmount(operation.getAmount()) +
                                     (operation.getType().equalsIgnoreCase("Deposit") ? " to " : " from ") + 
                                     accountNumber);
                    
                    if ("Deposit".equalsIgnoreCase(operation.getType())) {
                        account.deposit(operation.getAmount());
                        // Record transaction
                        Transaction transaction = new Transaction(
                                accountNumber,
                                "Deposit",
                                operation.getAmount(),
                                account.getBalance()
                        );
                        transactionManager.addTransaction(transaction);
                        return true;
                    } else if ("Withdrawal".equalsIgnoreCase(operation.getType())) {
                        boolean success = account.withdraw(operation.getAmount());
                        if (success) {
                            // Record transaction
                            Transaction transaction = new Transaction(
                                    accountNumber,
                                    "Withdrawal",
                                    operation.getAmount(),
                                    account.getBalance()
                            );
                            transactionManager.addTransaction(transaction);
                        }
                        return success;
                    }
                    return false;
                } catch (InvalidAmountException | InsufficientFundsException | OverdraftExceededException e) {
                    System.err.println("Error in " + Thread.currentThread().getName() + ": " + e.getMessage());
                    return false;
                }
            });
            futures.add(future);
        }
        
        // Wait for all threads to complete
        for (Future<Boolean> future : futures) {
            try {
                future.get(); // Wait for completion
            } catch (ExecutionException e) {
                System.err.println("Error executing transaction: " + e.getMessage());
            }
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        return account.getBalance();
    }
    
    /**
     * Simulates concurrent transactions using parallel streams for batch processing.
     * Uses parallel streams for concurrent execution.
     *
     * @param accountNumber the account number to perform transactions on
     * @param operations list of transaction operations to execute concurrently
     * @return the final balance after all transactions
     */
    public double simulateBatchTransactionsWithParallelStreams(String accountNumber, List<TransactionOperation> operations) {
        Account account = accountManager.findAccount(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        
        // Use parallel stream to process transactions concurrently
        operations.parallelStream().forEach(operation -> {
            try {
                String threadName = Thread.currentThread().getName();
                System.out.println(threadName + ": " + operation.getType() + 
                                 " $" + ValidationUtils.formatAmount(operation.getAmount()) + 
                                 " to " + accountNumber);
                
                if ("Deposit".equalsIgnoreCase(operation.getType())) {
                    account.deposit(operation.getAmount());
                    Transaction transaction = new Transaction(
                            accountNumber,
                            "Deposit",
                            operation.getAmount(),
                            account.getBalance()
                    );
                    transactionManager.addTransaction(transaction);
                } else if ("Withdrawal".equalsIgnoreCase(operation.getType())) {
                    boolean success = account.withdraw(operation.getAmount());
                    if (success) {
                        Transaction transaction = new Transaction(
                                accountNumber,
                                "Withdrawal",
                                operation.getAmount(),
                                account.getBalance()
                        );
                        transactionManager.addTransaction(transaction);
                    }
                }
            } catch (InvalidAmountException | InsufficientFundsException | OverdraftExceededException e) {
                System.err.println("Error in " + Thread.currentThread().getName() + ": " + e.getMessage());
            }
        });
        
        return account.getBalance();
    }
    
    /**
     * Creates a default set of transaction operations for demonstration.
     * 
     * @return list of transaction operations
     */
    public static List<TransactionOperation> createDefaultOperations() {
        List<TransactionOperation> operations = new ArrayList<>();
        operations.add(new TransactionOperation("Deposit", 500.0, 1));
        operations.add(new TransactionOperation("Deposit", 300.0, 2));
        operations.add(new TransactionOperation("Withdrawal", 200.0, 3));
        return operations;
    }
}

