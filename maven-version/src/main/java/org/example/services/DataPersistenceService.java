package org.example.services;

import org.example.models.Account;
import org.example.models.CheckingAccount;
import org.example.models.Customer;
import org.example.models.PremiumCustomer;
import org.example.models.RegularCustomer;
import org.example.models.SavingsAccount;
import org.example.models.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service class responsible for persisting accounts and transactions to files.
 * Uses Java NIO Files API and functional programming with Streams and method references.
 */
public class DataPersistenceService {
    private static final String DATA_DIR = "data";
    private static final String ACCOUNTS_FILE = "accounts.txt";
    private static final String TRANSACTIONS_FILE = "transactions.txt";
    
    private final AccountManager accountManager;
    private final TransactionManager transactionManager;
    
    public DataPersistenceService(AccountManager accountManager, TransactionManager transactionManager) {
        this.accountManager = accountManager;
        this.transactionManager = transactionManager;
        ensureDataDirectoryExists();
    }
    
    /**
     * Ensures the data directory exists, creating it if necessary.
     */
    private void ensureDataDirectoryExists() {
        try {
            Path dataDir = Paths.get(DATA_DIR);
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
        } catch (IOException e) {
            System.err.println("Error creating data directory: " + e.getMessage());
        }
    }
    
    /**
     * Gets the path to the accounts file.
     *
     * @return the Path to accounts.txt
     */
    private Path getAccountsFilePath() {
        return Paths.get(DATA_DIR, ACCOUNTS_FILE);
    }
    
    /**
     * Gets the path to the transactions file.
     *
     * @return the Path to transactions.txt
     */
    private Path getTransactionsFilePath() {
        return Paths.get(DATA_DIR, TRANSACTIONS_FILE);
    }
    
    /**
     * Loads accounts from file using Streams and method references.
     *
     * @return the number of accounts loaded
     */
    public int loadAccounts() {
        Path accountsPath = getAccountsFilePath();
        
        if (!Files.exists(accountsPath)) {
            return 0;
        }
        
        try (Stream<String> lines = Files.lines(accountsPath)) {
            List<Account> accounts = lines
                    .filter(line -> !line.trim().isEmpty())
                    .map(this::parseAccount)
                    .filter(account -> account != null)
                    .collect(Collectors.toList());
            
            accounts.forEach(account -> accountManager.addAccount(account, true));
            
            // Update account counter to highest account number to avoid conflicts
            if (!accounts.isEmpty()) {
                int maxCounter = accounts.stream()
                        .map(Account::getAccountNumber)
                        .mapToInt(accNum -> {
                            try {
                                String numPart = accNum.replace("ACC", "");
                                return Integer.parseInt(numPart);
                            } catch (NumberFormatException e) {
                                return 0;
                            }
                        })
                        .max()
                        .orElse(0);
                Account.setAccountCounter(maxCounter);
            }
            
            return accounts.size();
        } catch (IOException e) {
            System.err.println("Error loading accounts: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Loads transactions from file using Streams and method references.
     *
     * @return the number of transactions loaded
     */
    public int loadTransactions() {
        Path transactionsPath = getTransactionsFilePath();
        
        if (!Files.exists(transactionsPath)) {
            return 0;
        }
        
        try (Stream<String> lines = Files.lines(transactionsPath)) {
            List<Transaction> transactions = lines
                    .filter(line -> !line.trim().isEmpty())
                    .map(this::parseTransaction)
                    .filter(transaction -> transaction != null)
                    .collect(Collectors.toList());
            
            transactions.forEach(transactionManager::addTransaction);
            
            // Update transaction counter to highest transaction ID to avoid conflicts
            if (!transactions.isEmpty()) {
                int maxCounter = transactions.stream()
                        .map(Transaction::getTransactionID)
                        .mapToInt(txnId -> {
                            try {
                                String numPart = txnId.replace("TXN", "");
                                return Integer.parseInt(numPart);
                            } catch (NumberFormatException e) {
                                return 0;
                            }
                        })
                        .max()
                        .orElse(0);
                Transaction.setTransactionCounter(maxCounter);
            }
            
            return transactions.size();
        } catch (IOException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Saves all accounts to file.
     *
     * @return true if successful, false otherwise
     */
    public boolean saveAccounts() {
        Path accountsPath = getAccountsFilePath();
        
        try {
            // Use Streams to convert accounts to lines
            List<String> lines = accountManager.getAllAccounts().values().stream()
                    .map(this::serializeAccount)
                    .collect(Collectors.toList());
            
            Files.write(accountsPath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving accounts: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Saves all transactions to file.
     *
     * @return true if successful, false otherwise
     */
    public boolean saveTransactions() {
        Path transactionsPath = getTransactionsFilePath();
        
        try {
            // Use Streams to convert transactions to lines
            List<String> lines = transactionManager.getAllTransactions().stream()
                    .map(this::serializeTransaction)
                    .collect(Collectors.toList());
            
            Files.write(transactionsPath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving transactions: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Saves both accounts and transactions.
     *
     * @return true if both operations succeeded, false otherwise
     */
    public boolean saveAll() {
        boolean accountsSaved = saveAccounts();
        boolean transactionsSaved = saveTransactions();
        return accountsSaved && transactionsSaved;
    }
    
    /**
     * Serializes an account to a string format.
     * Format: AccountType|AccountNumber|CustomerType|Name|Age|Contact|Address|Balance|AccountSpecificData
     * For SavingsAccount: AccountSpecificData = InterestRate,MinimumBalance
     * For CheckingAccount: AccountSpecificData = OverdraftLimit
     *
     * @param account the account to serialize
     * @return the serialized string representation
     */
    private String serializeAccount(Account account) {
        Customer customer = account.getCustomer();
        StringBuilder sb = new StringBuilder();
        
        sb.append(account.getAccountType()).append("|");
        sb.append(account.getAccountNumber()).append("|");
        sb.append(customer.getCustomerType()).append("|");
        sb.append(customer.getName()).append("|");
        sb.append(customer.getAge()).append("|");
        sb.append(customer.getContact()).append("|");
        sb.append(customer.getAddress()).append("|");
        sb.append(account.getBalance()).append("|");
        
        if (account instanceof SavingsAccount) {
            SavingsAccount savings = (SavingsAccount) account;
            sb.append(savings.getInterestRate()).append(",").append(savings.getMinimumBalance());
        } else if (account instanceof CheckingAccount) {
            CheckingAccount checking = (CheckingAccount) account;
            sb.append(checking.getOverdraftLimit());
        }
        
        return sb.toString();
    }
    
    /**
     * Parses a line into an Account object.
     *
     * @param line the line to parse
     * @return the parsed Account, or null if parsing fails
     */
    private Account parseAccount(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length < 8) {
                return null;
            }
            
            String accountType = parts[0];
            String accountNumber = parts[1];
            String customerType = parts[2];
            String customerName = parts[3];
            int age = Integer.parseInt(parts[4]);
            String contact = parts[5];
            String address = parts[6];
            double balance = Double.parseDouble(parts[7]);
            
            // Create customer
            Customer customer;
            if ("Premium".equals(customerType)) {
                customer = new PremiumCustomer(customerName, age, contact, address);
            } else {
                customer = new RegularCustomer(customerName, age, contact, address);
            }
            
            // Create account based on type with initial balance
            Account account;
            if ("Savings".equals(accountType)) {
                account = new SavingsAccount(customer, balance);
            } else if ("Checking".equals(accountType)) {
                account = new CheckingAccount(customer, balance);
            } else {
                return null;
            }
            
            // Set account number manually since constructor auto-generates it
            setAccountNumber(account, accountNumber);
            
            return account;
        } catch (Exception e) {
            System.err.println("Error parsing account line: " + line + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Serializes a transaction to a string format.
     * Format: TransactionID|AccountNumber|Type|Amount|BalanceAfter|DateTime
     *
     * @param transaction the transaction to serialize
     * @return the serialized string representation
     */
    private String serializeTransaction(Transaction transaction) {
        return String.join("|",
                transaction.getTransactionID(),
                transaction.getAccountNumber(),
                transaction.getType(),
                String.valueOf(transaction.getAmount()),
                String.valueOf(transaction.getBalance()),
                transaction.getDateTime()
        );
    }
    
    /**
     * Parses a line into a Transaction object using method reference pattern.
     *
     * @param line the line to parse
     * @return the parsed Transaction, or null if parsing fails
     */
    private Transaction parseTransaction(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length != 6) {
                return null;
            }
            
            String accountNumber = parts[1];
            String type = parts[2];
            double amount = Double.parseDouble(parts[3]);
            double balanceAfter = Double.parseDouble(parts[4]);
            
            // Create transaction
            Transaction transaction = new Transaction(accountNumber, type, amount, balanceAfter);
            
            // Set transaction ID and timestamp manually
            setTransactionId(transaction, parts[0]);
            setTransactionTimestamp(transaction, parts[5]);
            
            return transaction;
        } catch (Exception e) {
            System.err.println("Error parsing transaction line: " + line + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Sets the account number using reflection (since Account doesn't have a setter).
     */
    private void setAccountNumber(Account account, String accountNumber) {
        try {
            java.lang.reflect.Field field = Account.class.getDeclaredField("accountNumber");
            field.setAccessible(true);
            field.set(account, accountNumber);
        } catch (Exception e) {
            System.err.println("Error setting account number: " + e.getMessage());
        }
    }
    
    /**
     * Sets the transaction ID using reflection.
     */
    private void setTransactionId(Transaction transaction, String transactionId) {
        try {
            java.lang.reflect.Field field = Transaction.class.getDeclaredField("transactionId");
            field.setAccessible(true);
            field.set(transaction, transactionId);
        } catch (Exception e) {
            System.err.println("Error setting transaction ID: " + e.getMessage());
        }
    }
    
    /**
     * Sets the transaction timestamp using reflection.
     */
    private void setTransactionTimestamp(Transaction transaction, String timestamp) {
        try {
            java.lang.reflect.Field timestampField = Transaction.class.getDeclaredField("timestamp");
            timestampField.setAccessible(true);
            timestampField.set(transaction, timestamp);
            
            // Also parse and set the LocalDateTime field
            java.lang.reflect.Field dateTimeField = Transaction.class.getDeclaredField("dateTime");
            dateTimeField.setAccessible(true);
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
            java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(timestamp, formatter);
            dateTimeField.set(transaction, dateTime);
        } catch (Exception e) {
            System.err.println("Error setting transaction timestamp: " + e.getMessage());
        }
    }
}
