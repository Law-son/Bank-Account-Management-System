package org.example.services;

import org.example.models.Account;
import org.example.models.CheckingAccount;
import org.example.models.Customer;
import org.example.models.PremiumCustomer;
import org.example.models.RegularCustomer;
import org.example.models.SavingsAccount;
import org.example.models.Transaction;
import org.example.utils.ValidationUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
                    .filter(Objects::nonNull)
                    .toList();
            
            accounts.forEach(account -> accountManager.addAccount(account, true));
            
            // Update account counter to the highest account number to avoid conflicts
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
                    .filter(Objects::nonNull)
                    .toList();
            
            transactions.forEach(transactionManager::addTransaction);
            
            // Update transaction counter to the highest transaction ID to avoid conflicts
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
     * Validates data before saving to ensure integrity.
     *
     * @return true if successful, false otherwise
     */
    public boolean saveAccounts() {
        Path accountsPath = getAccountsFilePath();
        
        try {
            // Validate all accounts before saving
            List<Account> accountsToSave = accountManager.getAllAccounts().values().stream()
                    .filter(this::validateAccount)
                    .toList();
            
            if (accountsToSave.isEmpty() && accountManager.getAccountCount() > 0) {
                System.err.println("Warning: No valid accounts to save after validation.");
                return false;
            }
            
            // Use Streams to convert accounts to lines
            List<String> lines = accountsToSave.stream()
                    .map(this::serializeAccount)
                    .toList();
            
            Files.write(accountsPath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving accounts: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Saves all transactions to file.
     * Validates data before saving to ensure integrity.
     *
     * @return true if successful, false otherwise
     */
    public boolean saveTransactions() {
        Path transactionsPath = getTransactionsFilePath();
        
        try {
            // Validate all transactions before saving
            List<Transaction> transactionsToSave = transactionManager.getAllTransactions().stream()
                    .filter(this::validateTransaction)
                    .toList();
            
            // Use Streams to convert transactions to lines
            List<String> lines = transactionsToSave.stream()
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
        
        if (account instanceof SavingsAccount savings) {
            sb.append(savings.getInterestRate()).append(",").append(savings.getMinimumBalance());
        } else if (account instanceof CheckingAccount checking) {
            sb.append(checking.getOverdraftLimit());
        }
        
        return sb.toString();
    }
    
    /**
     * Validates an account before saving.
     *
     * @param account the account to validate
     * @return true if valid, false otherwise
     */
    private boolean validateAccount(Account account) {
        if (account == null) {
            return false;
        }
        
        // Validate account number format
        if (!ValidationUtils.isValidAccountNumber(account.getAccountNumber())) {
            System.err.println("Invalid account number format: " + account.getAccountNumber());
            return false;
        }
        
        // Validate customer contact
        Customer customer = account.getCustomer();
        if (customer != null && !ValidationUtils.isValidPhoneNumber(customer.getContact())) {
            System.err.println("Invalid phone number format for account " + account.getAccountNumber() + ": " + customer.getContact());
            return false;
        }
        
        // Validate balance is not negative (checking accounts can have negative balance due to overdraft)
        // But we'll still validate it's a valid number
        if (Double.isNaN(account.getBalance()) || Double.isInfinite(account.getBalance())) {
            System.err.println("Invalid balance for account " + account.getAccountNumber());
            return false;
        }
        
        // Validate savings account minimum balance
        if (account instanceof SavingsAccount savings) {
            if (savings.getBalance() < savings.getMinimumBalance()) {
                System.err.println("Warning: Account " + account.getAccountNumber() + 
                                 " balance is below minimum, but will still save.");
            }
        }
        
        return true;
    }
    
    /**
     * Validates a transaction before saving.
     *
     * @param transaction the transaction to validate
     * @return true if valid, false otherwise
     */
    private boolean validateTransaction(Transaction transaction) {
        if (transaction == null) {
            return false;
        }
        
        // Validate account number format
        if (!ValidationUtils.isValidAccountNumber(transaction.getAccountNumber())) {
            System.err.println("Invalid account number in transaction: " + transaction.getTransactionID());
            return false;
        }
        
        // Validate amount is a valid number
        if (Double.isNaN(transaction.getAmount()) || Double.isInfinite(transaction.getAmount())) {
            System.err.println("Invalid amount in transaction: " + transaction.getTransactionID());
            return false;
        }
        
        // Validate balance after is a valid number
        if (Double.isNaN(transaction.getBalance()) || Double.isInfinite(transaction.getBalance())) {
            System.err.println("Invalid balance after in transaction: " + transaction.getTransactionID());
            return false;
        }
        
        // Verify account exists for this transaction
        Account account = accountManager.findAccount(transaction.getAccountNumber());
        if (account == null) {
            System.err.println("Warning: Transaction " + transaction.getTransactionID() + 
                             " references non-existent account: " + transaction.getAccountNumber());
        }
        
        return true;
    }
    
    /**
     * Parses a line into an Account object.
     * Validates data during parsing.
     *
     * @param line the line to parse
     * @return the parsed Account, or null if parsing fails or validation fails
     */
    private Account parseAccount(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length < 8) {
                System.err.println("Invalid account line format: insufficient parts");
                return null;
            }
            
            String accountType = parts[0];
            String accountNumber = parts[1];
            String customerType = parts[2];
            String customerName = parts[3];
            
            // Validate account number format
            if (!ValidationUtils.isValidAccountNumber(accountNumber)) {
                System.err.println("Invalid account number format in line: " + accountNumber);
                return null;
            }
            
            // Validate and parse age
            int age;
            try {
                age = Integer.parseInt(parts[4]);
                if (age <= 0 || age > 150) {
                    System.err.println("Invalid age in account line: " + age);
                    return null;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid age format in account line: " + parts[4]);
                return null;
            }
            
            String contact = parts[5];
            String address = parts[6];
            
            // Validate phone number format
            if (!ValidationUtils.isValidPhoneNumber(contact)) {
                System.err.println("Invalid phone number format in account line: " + contact);
                return null;
            }
            
            // Validate address
            if (address == null || address.trim().isEmpty()) {
                System.err.println("Invalid address in account line: empty address");
                return null;
            }
            
            // Validate and parse balance
            double balance;
            try {
                balance = Double.parseDouble(parts[7]);
                if (Double.isNaN(balance) || Double.isInfinite(balance)) {
                    System.err.println("Invalid balance in account line: " + parts[7]);
                    return null;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid balance format in account line: " + parts[7]);
                return null;
            }
            
            // Validate customer type
            if (!"Premium".equals(customerType) && !"Regular".equals(customerType)) {
                System.err.println("Invalid customer type in account line: " + customerType);
                return null;
            }
            
            // Create customer
            Customer customer;
            if ("Premium".equals(customerType)) {
                customer = new PremiumCustomer(customerName, age, contact, address);
            } else {
                customer = new RegularCustomer(customerName, age, contact, address);
            }
            
            // Validate account type and create account
            Account account;
            if ("Savings".equals(accountType)) {
                account = new SavingsAccount(customer, balance);
            } else if ("Checking".equals(accountType)) {
                account = new CheckingAccount(customer, balance);
            } else {
                System.err.println("Invalid account type in account line: " + accountType);
                return null;
            }
            
            // Set account number manually since constructor auto-generates it
            setAccountNumber(account, accountNumber);
            
            // Final validation
            if (!validateAccount(account)) {
                return null;
            }
            
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
     * Validates data during parsing.
     *
     * @param line the line to parse
     * @return the parsed Transaction, or null if parsing fails or validation fails
     */
    private Transaction parseTransaction(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length != 6) {
                System.err.println("Invalid transaction line format: expected 6 parts, got " + parts.length);
                return null;
            }
            
            String transactionId = parts[0];
            String accountNumber = parts[1];
            String type = parts[2];
            
            // Validate account number format
            if (!ValidationUtils.isValidAccountNumber(accountNumber)) {
                System.err.println("Invalid account number format in transaction line: " + accountNumber);
                return null;
            }
            
            // Validate transaction type
            if (!"Deposit".equalsIgnoreCase(type) && 
                !"Withdrawal".equalsIgnoreCase(type) &&
                !"Transfer In".equalsIgnoreCase(type) &&
                !"Transfer Out".equalsIgnoreCase(type)) {
                System.err.println("Invalid transaction type in line: " + type);
                return null;
            }
            
            // Validate and parse amount
            double amount;
            try {
                amount = Double.parseDouble(parts[3]);
                if (Double.isNaN(amount) || Double.isInfinite(amount)) {
                    System.err.println("Invalid amount in transaction line: " + parts[3]);
                    return null;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid amount format in transaction line: " + parts[3]);
                return null;
            }
            
            // Validate and parse balance after
            double balanceAfter;
            try {
                balanceAfter = Double.parseDouble(parts[4]);
                if (Double.isNaN(balanceAfter) || Double.isInfinite(balanceAfter)) {
                    System.err.println("Invalid balance after in transaction line: " + parts[4]);
                    return null;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid balance after format in transaction line: " + parts[4]);
                return null;
            }
            
            String dateTime = parts[5];
            
            // Create transaction
            Transaction transaction = new Transaction(accountNumber, type, amount, balanceAfter);
            
            // Set transaction ID and timestamp manually
            setTransactionId(transaction, transactionId);
            setTransactionTimestamp(transaction, dateTime);
            
            // Final validation
            if (!validateTransaction(transaction)) {
                return null;
            }
            
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
