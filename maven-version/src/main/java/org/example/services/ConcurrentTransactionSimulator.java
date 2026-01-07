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
     * Note: Operations are ordered to prevent insufficient funds errors:
     * - All deposits execute first (concurrently)
     * - Then all withdrawals execute (concurrently)
     * This ensures withdrawals have sufficient funds from deposits.
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
        
        // Separate deposits and withdrawals to prevent insufficient funds errors
        // Deposits execute first, then withdrawals
        List<TransactionOperation> deposits = new ArrayList<>();
        List<TransactionOperation> withdrawals = new ArrayList<>();
        
        for (TransactionOperation op : operations) {
            if ("Deposit".equalsIgnoreCase(op.getType())) {
                deposits.add(op);
            } else if ("Withdrawal".equalsIgnoreCase(op.getType())) {
                withdrawals.add(op);
            }
        }
        
        // Debug: Print operations being processed
        System.out.println("\nProcessing " + deposits.size() + " deposit(s) and " + withdrawals.size() + " withdrawal(s)");
        double expectedChange = deposits.stream().mapToDouble(TransactionOperation::getAmount).sum() -
                               withdrawals.stream().mapToDouble(TransactionOperation::getAmount).sum();
        System.out.println("Expected balance change: " + 
                          (expectedChange >= 0 ? "+" : "") + 
                          ValidationUtils.formatAmount(expectedChange));
        
        // Phase 1: Execute all deposits concurrently
        if (!deposits.isEmpty()) {
            // Create separate thread factory for deposits
            ThreadFactory depositThreadFactory = new ThreadFactory() {
                private int threadNumber = 1;
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "Thread-" + threadNumber++);
                }
            };
            
            ExecutorService depositExecutor = Executors.newFixedThreadPool(deposits.size(), depositThreadFactory);
            List<Future<Boolean>> depositFutures = new ArrayList<>();
            
            for (TransactionOperation operation : deposits) {
                // Capture operation details for logging
                final double depositAmount = operation.getAmount();
                Future<Boolean> future = depositExecutor.submit(() -> {
                    try {
                        String threadName = Thread.currentThread().getName();
                        double balanceBefore = account.getBalance();
                        System.out.println(threadName + ": Depositing " +
                                         ValidationUtils.formatAmount(depositAmount) +
                                         " to " + accountNumber + " (Balance before: " +
                                         ValidationUtils.formatAmount(balanceBefore) + ")");
                        
                        // Synchronize on account to ensure atomic operation + balance retrieval
                        // This prevents race conditions where balance is read after other threads modify it
                        synchronized (account) {
                            double balanceBeforeOp = account.getBalance();
                            account.deposit(depositAmount);
                            // Get balance immediately after deposit within the same lock
                            double balanceAfter = account.getBalance();
                            
                            // Verify the deposit actually happened
                            if (Math.abs(balanceAfter - (balanceBeforeOp + depositAmount)) > 0.01) {
                                System.err.println("ERROR: Deposit mismatch! Expected balance: " +
                                                 ValidationUtils.formatAmount(balanceBeforeOp + depositAmount) +
                                                 ", Actual: " + ValidationUtils.formatAmount(balanceAfter));
                            }
                            
                            // Record transaction
                            Transaction transaction = new Transaction(
                                    accountNumber,
                                    "Deposit",
                                    depositAmount,
                                    balanceAfter
                            );
                            transactionManager.addTransaction(transaction);
                            System.out.println(threadName + ": Deposit completed. New balance: " +
                                             ValidationUtils.formatAmount(balanceAfter));
                            return true;
                        }
                    } catch (InvalidAmountException e) {
                        System.err.println("Error in " + Thread.currentThread().getName() + ": " + e.getMessage());
                        return false;
                    } catch (Exception e) {
                        System.err.println("Unexpected error in " + Thread.currentThread().getName() + ": " + e.getMessage());
                        e.printStackTrace();
                        return false;
                    }
                });
                depositFutures.add(future);
            }
            
            // Wait for all deposits to complete and verify they succeeded
            for (int i = 0; i < depositFutures.size(); i++) {
                try {
                    Boolean success = depositFutures.get(i).get();
                    if (!success) {
                        System.err.println("Warning: Deposit operation " + (i + 1) + " failed!");
                    }
                } catch (ExecutionException e) {
                    System.err.println("Error executing deposit " + (i + 1) + ": " + e.getMessage());
                    if (e.getCause() != null) {
                        System.err.println("  Cause: " + e.getCause().getMessage());
                    }
                }
            }
            
            depositExecutor.shutdown();
            depositExecutor.awaitTermination(5, TimeUnit.SECONDS);
        }
        
        // Phase 2: Execute all withdrawals concurrently (after deposits complete)
        if (!withdrawals.isEmpty()) {
            // Create separate thread factory for withdrawals (continues numbering from deposits)
            ThreadFactory withdrawalThreadFactory = new ThreadFactory() {
                private int threadNumber = deposits.size() + 1;
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "Thread-" + threadNumber++);
                }
            };
            
            ExecutorService withdrawalExecutor = Executors.newFixedThreadPool(withdrawals.size(), withdrawalThreadFactory);
            List<Future<Boolean>> withdrawalFutures = new ArrayList<>();
            
            for (TransactionOperation operation : withdrawals) {
                Future<Boolean> future = withdrawalExecutor.submit(() -> {
                    try {
                        String threadName = Thread.currentThread().getName();
                        System.out.println(threadName + ": Withdrawing " +
                                         ValidationUtils.formatAmount(operation.getAmount()) +
                                         " from " + accountNumber);
                        
                        // Synchronize on account to ensure atomic operation + balance retrieval
                        synchronized (account) {
                            double balanceBefore = account.getBalance();
                            boolean success = account.withdraw(operation.getAmount());
                            if (success) {
                                // Get balance immediately after withdrawal within the same lock
                                double balanceAfter = account.getBalance();
                                // Record transaction
                                Transaction transaction = new Transaction(
                                        accountNumber,
                                        "Withdrawal",
                                        operation.getAmount(),
                                        balanceAfter
                                );
                                transactionManager.addTransaction(transaction);
                                System.out.println(threadName + ": Withdrawal completed. Balance before: " +
                                                 ValidationUtils.formatAmount(balanceBefore) +
                                                 ", Balance after: " + ValidationUtils.formatAmount(balanceAfter));
                            }
                            return success;
                        }
                    } catch (InsufficientFundsException | OverdraftExceededException e) {
                        System.err.println("Error in " + Thread.currentThread().getName() + ": " + e.getMessage());
                        return false;
                    }
                });
                withdrawalFutures.add(future);
            }
            
            // Wait for all withdrawals to complete and verify they succeeded
            for (int i = 0; i < withdrawalFutures.size(); i++) {
                try {
                    Boolean success = withdrawalFutures.get(i).get();
                    if (!success) {
                        System.err.println("Warning: Withdrawal operation " + (i + 1) + " failed!");
                    }
                } catch (ExecutionException e) {
                    System.err.println("Error executing withdrawal " + (i + 1) + ": " + e.getMessage());
                    if (e.getCause() != null) {
                        System.err.println("  Cause: " + e.getCause().getMessage());
                    }
                }
            }
            
            withdrawalExecutor.shutdown();
            withdrawalExecutor.awaitTermination(5, TimeUnit.SECONDS);
        }
        
        double finalBalance = account.getBalance();
        
        // Print summary
        System.out.println("\n--- Transaction Summary ---");
        System.out.println("Initial Balance: " + ValidationUtils.formatAmount(initialBalance));
        System.out.println("Deposits: " + deposits.size() + " transaction(s) totaling " + 
                          ValidationUtils.formatAmount(deposits.stream().mapToDouble(TransactionOperation::getAmount).sum()));
        System.out.println("Withdrawals: " + withdrawals.size() + " transaction(s) totaling " + 
                          ValidationUtils.formatAmount(withdrawals.stream().mapToDouble(TransactionOperation::getAmount).sum()));
        System.out.println("Final Balance: " + ValidationUtils.formatAmount(finalBalance));
        System.out.println("Balance Change: " + 
                          (finalBalance >= initialBalance ? "+" : "") + 
                          ValidationUtils.formatAmount(finalBalance - initialBalance));
        System.out.println("Expected Change: " + 
                          (expectedChange >= 0 ? "+" : "") + 
                          ValidationUtils.formatAmount(expectedChange));
        
        // Verify the balance is correct
        double calculatedFinal = initialBalance + 
                                 deposits.stream().mapToDouble(TransactionOperation::getAmount).sum() -
                                 withdrawals.stream().mapToDouble(TransactionOperation::getAmount).sum();
        if (Math.abs(finalBalance - calculatedFinal) > 0.01) {
            System.err.println("\n  WARNING: Balance mismatch! Calculated: " +
                             ValidationUtils.formatAmount(calculatedFinal) + 
                             ", Actual: " + ValidationUtils.formatAmount(finalBalance));
        } else {
            System.out.println("Balance verification: PASSED");
        }
        
        return finalBalance;
    }
    
    /**
     * Simulates concurrent transactions using parallel streams for batch processing.
     * Uses parallel streams for concurrent execution.
     * 
     * Note: Operations are ordered to prevent insufficient funds errors:
     * - All deposits execute first (concurrently)
     * - Then all withdrawals execute (concurrently)
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
        
        // Separate deposits and withdrawals to prevent insufficient funds errors
        List<TransactionOperation> deposits = operations.stream()
                .filter(op -> "Deposit".equalsIgnoreCase(op.getType()))
                .collect(java.util.stream.Collectors.toList());
        List<TransactionOperation> withdrawals = operations.stream()
                .filter(op -> "Withdrawal".equalsIgnoreCase(op.getType()))
                .collect(java.util.stream.Collectors.toList());
        
        // Phase 1: Execute all deposits concurrently using parallel stream
        deposits.parallelStream().forEach(operation -> {
            try {
                String threadName = Thread.currentThread().getName();
                System.out.println(threadName + ": Depositing $" + 
                                 ValidationUtils.formatAmount(operation.getAmount()) + 
                                 " to " + accountNumber);
                
                // Synchronize on account to ensure atomic operation + balance retrieval
                synchronized (account) {
                    account.deposit(operation.getAmount());
                    // Get balance immediately after deposit within the same lock
                    double balanceAfter = account.getBalance();
                    Transaction transaction = new Transaction(
                            accountNumber,
                            "Deposit",
                            operation.getAmount(),
                            balanceAfter
                    );
                    transactionManager.addTransaction(transaction);
                }
            } catch (InvalidAmountException e) {
                System.err.println("Error in " + Thread.currentThread().getName() + ": " + e.getMessage());
            }
        });
        
        // Phase 2: Execute all withdrawals concurrently (after deposits complete)
        withdrawals.parallelStream().forEach(operation -> {
            try {
                String threadName = Thread.currentThread().getName();
                System.out.println(threadName + ": Withdrawing $" + 
                                 ValidationUtils.formatAmount(operation.getAmount()) + 
                                 " from " + accountNumber);
                
                // Synchronize on account to ensure atomic operation + balance retrieval
                synchronized (account) {
                    boolean success = account.withdraw(operation.getAmount());
                    if (success) {
                        // Get balance immediately after withdrawal within the same lock
                        double balanceAfter = account.getBalance();
                        Transaction transaction = new Transaction(
                                accountNumber,
                                "Withdrawal",
                                operation.getAmount(),
                                balanceAfter
                        );
                        transactionManager.addTransaction(transaction);
                    }
                }
            } catch (InsufficientFundsException | OverdraftExceededException e) {
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
        operations.add(new TransactionOperation("Deposit", 200.0, 1));
        operations.add(new TransactionOperation("Deposit", 100.0, 2));
        operations.add(new TransactionOperation("Withdrawal", 10000.0, 3));
        return operations;
    }
}

