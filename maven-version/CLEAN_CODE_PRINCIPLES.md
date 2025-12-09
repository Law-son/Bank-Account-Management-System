# Clean Code Principles Applied to Bank Account Management System

This document explains the clean code principles applied during the refactoring, with specific examples from the codebase.

---

## 1. **SOLID Principles**

### **S - Single Responsibility Principle (SRP)**

**What it means:** Each class should have only one reason to change - it should have a single, well-defined responsibility.

**Examples in the code:**

1. **`TransferService.java`** (Lines 13-85)
   - **Responsibility:** Handles ONLY money transfers between accounts
   - **Why:** Instead of putting transfer logic in `Main.java` or `TransactionManager`, we created a dedicated service
   - **Benefit:** If transfer logic needs to change, we only modify one class

```java
public class TransferService {
    // Only handles transfers - nothing else
    public void transfer(String fromAccountNumber, String toAccountNumber, double amount)
}
```

2. **`StatementGenerator.java`**
   - **Responsibility:** ONLY generates account statements
   - **Why:** Separated from `TransactionManager` which handles transaction storage
   - **Benefit:** Statement formatting can change without affecting transaction management

3. **`Main.java` - Extracted Methods**
   - **Before:** One giant method handling all transaction types
   - **After:** Separate methods for each transaction type:
     - `handleDeposit()` - only handles deposits
     - `handleWithdrawal()` - only handles withdrawals  
     - `handleTransfer()` - only handles transfers
   - **Why:** Each method has one clear purpose

---

### **O - Open/Closed Principle (OCP)**

**What it means:** Classes should be open for extension but closed for modification.

**Examples in the code:**

1. **Account Hierarchy** (`Account.java`, `SavingsAccount.java`, `CheckingAccount.java`)
   - **How:** The abstract `Account` class defines the contract
   - **Extension:** New account types (e.g., `BusinessAccount`) can extend `Account` without modifying existing code
   - **Why:** Each account type implements `withdraw()` differently, but the interface remains the same

```java
// Base class - closed for modification
public abstract class Account {
    public abstract void withdraw(double amount) throws InsufficientFundsException;
}

// Extensions - open for new account types
public class SavingsAccount extends Account { ... }
public class CheckingAccount extends Account { ... }
```

---

### **L - Liskov Substitution Principle (LSP)**

**What it means:** Subtypes must be substitutable for their base types without breaking the program.

**Examples in the code:**

1. **Account Subtypes**
   - **How:** Both `SavingsAccount` and `CheckingAccount` can be used wherever `Account` is expected
   - **Example:** In `Main.java` line 225, we use `Account account = findAccountOrShowError(...)`
   - **Why:** The code works with the base type, and any account subtype can be substituted

```java
// Works with any Account subtype
Account account = accountManager.findAccount(accountNumber);
// Could be SavingsAccount or CheckingAccount - doesn't matter!
```

---

### **I - Interface Segregation Principle (ISP)**

**What it means:** Clients should not be forced to depend on interfaces they don't use.

**Examples in the code:**

1. **`Transactable` Interface**
   - **How:** Only contains methods related to transactions (`processTransaction`)
   - **Why:** Accounts don't need to implement unrelated methods
   - **Benefit:** If we add other interfaces (e.g., `Reportable`), accounts can choose which to implement

---

### **D - Dependency Inversion Principle (DIP)**

**What it means:** High-level modules should not depend on low-level modules. Both should depend on abstractions.

**Examples in the code:**

1. **`TransferService` Constructor** (Lines 23-26)
   - **How:** `TransferService` depends on `AccountManager` and `TransactionManager` interfaces, not concrete implementations
   - **Why:** Dependencies are injected through constructor
   - **Benefit:** Easy to test (can inject mock objects) and swap implementations

```java
public TransferService(AccountManager accountManager, TransactionManager transactionManager) {
    this.accountManager = accountManager;  // Depends on abstraction
    this.transactionManager = transactionManager;  // Not concrete implementation
}
```

---

## 2. **DRY (Don't Repeat Yourself)**

**What it means:** Avoid code duplication - extract common logic into reusable methods.

**Examples in the code:**

### **Before Refactoring:**
```java
// This pattern was repeated 3+ times:
Account account = accountManager.findAccount(accountNumber);
if (account == null) {
    System.out.println("Account not found.");
    InputValidator.getString("Press Enter to continue");
    menuStack.pop();
    return;
}
```

### **After Refactoring:**
```java
// Extracted to reusable method (Lines 257-265)
private static Account findAccountOrShowError(String accountNumber) {
    Account account = accountManager.findAccount(accountNumber);
    if (account == null) {
        displayError("Account not found. Please check the account number.");
        ValidationUtils.getString("Press Enter to continue");
        menuStack.pop();
    }
    return account;
}

// Now used in multiple places:
Account account = findAccountOrShowError(accountNumber);  // Line 225
Account toAccount = findAccountOrShowError(toAccountNumber);  // Line 383
```

**Other DRY Examples:**

1. **`displayError()` Method** (Lines 457-459)
   - **Before:** `System.out.println("Error: " + message);` repeated everywhere
   - **After:** Single method called `displayError(message)`
   - **Used in:** Lines 260, 303, 337, 389, 416, 443

2. **`displayAccountDetails()` Method** (Lines 272-277)
   - **Before:** Account details display code duplicated
   - **After:** Single method reused

3. **`createCustomer()` and `createAccount()` Methods** (Lines 172-209)
   - **Before:** Customer/account creation logic mixed with menu code
   - **After:** Extracted to separate methods for reusability

---

## 3. **Consistent Naming Conventions**

**What it means:** Use clear, descriptive names that follow Java conventions consistently.

**Examples in the code:**

1. **Method Naming:**
   - All methods use camelCase: `handleDeposit()`, `handleWithdrawal()`, `findAccountOrShowError()`
   - Methods that return booleans: `processTransaction()` (returns boolean)
   - Methods that perform actions: `transfer()`, `deposit()`, `withdraw()`

2. **Variable Naming:**
   - Descriptive names: `fromAccountNumber`, `toAccountNumber` (not `acc1`, `acc2`)
   - Consistent patterns: `accountNumber` (not `accNum` or `accountNum`)

3. **Class Naming:**
   - Services end with "Service": `TransferService`, `AccountManager` (manager is also a service)
   - Exceptions end with "Exception": `AccountNotFoundException`, `InsufficientFundsException`
   - Utils end with "Utils": `ValidationUtils`

---

## 4. **JavaDoc Documentation**

**What it means:** Document all public APIs and complex logic with clear JavaDoc comments.

**Examples in the code:**

1. **Class-Level JavaDoc:**
```java
/**
 * Service class responsible for handling money transfers between accounts.
 * Follows Single Responsibility Principle by isolating transfer logic.
 */
public class TransferService { ... }
```

2. **Method-Level JavaDoc:**
```java
/**
 * Transfers money from one account to another.
 *
 * @param fromAccountNumber the account number to transfer from
 * @param toAccountNumber the account number to transfer to
 * @param amount the amount to transfer
 * @throws AccountNotFoundException if either account is not found
 * @throws InvalidAmountException if the amount is invalid
 * @throws InsufficientFundsException if the source account has insufficient funds
 */
public void transfer(...) { ... }
```

3. **Why it matters:**
   - Makes code self-documenting
   - IDE can show tooltips with method descriptions
   - Helps other developers understand code without reading implementation

---

## 5. **Method Extraction (Small Methods)**

**What it means:** Break large methods into smaller, focused methods that do one thing well.

**Examples in the code:**

### **Before:**
```java
// One giant method handling all transaction logic (100+ lines)
private static void processTransactionMenu() {
    // Account lookup
    // Validation
    // Deposit logic
    // Withdrawal logic
    // Transfer logic
    // All mixed together!
}
```

### **After:**
```java
// Main method delegates to smaller methods (Lines 214-249)
private static void processTransactionMenu() {
    Account account = findAccountOrShowError(accountNumber);
    displayAccountDetails(account);
    
    switch (transactionType) {
        case 1: handleDeposit(account); break;      // 24 lines
        case 2: handleWithdrawal(account); break;   // 27 lines
        case 3: handleTransfer(account); break;      // 45 lines
    }
}

// Each method is focused and testable
private static void handleDeposit(Account account) { ... }
private static void handleWithdrawal(Account account) { ... }
private static void handleTransfer(Account account) { ... }
```

**Benefits:**
- Each method is easier to understand
- Each method is easier to test
- Each method is easier to modify
- Methods can be reused

---

## 6. **Error Handling with Custom Exceptions**

**What it means:** Use specific exceptions instead of generic error messages or boolean returns.

**Examples in the code:**

### **Before:**
```java
// Generic error handling
if (account == null) {
    System.out.println("Account not found.");
    return;
}
```

### **After:**
```java
// Specific exceptions with standardized messages
if (account == null) {
    throw new AccountNotFoundException("Account not found. Please check the account number.");
}
```

**Custom Exceptions Created:**
1. `AccountNotFoundException` - when account lookup fails
2. `InvalidAmountException` - when amount is invalid (≤ 0)
3. `InsufficientFundsException` - when account lacks funds
4. `OverdraftExceededException` - when overdraft limit exceeded

**Benefits:**
- Type-safe error handling
- Can catch specific exceptions
- Standardized error messages
- Better debugging (stack traces)

---

## 7. **Separation of Concerns**

**What it means:** Different aspects of the application should be handled by different modules.

**Examples in the code:**

1. **UI Layer (`Main.java`):**
   - Handles: Menu display, user input, navigation
   - Does NOT handle: Business logic, validation rules

2. **Service Layer (`TransferService`, `AccountManager`, etc.):**
   - Handles: Business logic, operations
   - Does NOT handle: UI, user interaction

3. **Model Layer (`Account`, `Customer`, `Transaction`):**
   - Handles: Data representation, domain rules
   - Does NOT handle: UI or service orchestration

4. **Exception Layer (`exceptions/`):**
   - Handles: Error definitions
   - Does NOT handle: Error display (that's UI's job)

**Flow Example:**
```
User Input (Main.java) 
    → Service Layer (TransferService) 
        → Model Layer (Account.withdraw()) 
            → Exception (if error)
                → Back to UI (Main.java displays error)
```

---

## 8. **Constants and Magic Numbers**

**What it means:** Avoid hardcoded values; use named constants.

**Examples in the code:**

1. **Menu Options:**
```java
// Instead of: if (choice == 1) ...
// We use: case 1: handleDeposit(...)
// But could be improved with:
private static final int DEPOSIT = 1;
private static final int WITHDRAWAL = 2;
private static final int TRANSFER = 3;
```

2. **Account Types:**
```java
// In SavingsAccount and CheckingAccount:
public static final double DEFAULT_INTEREST_RATE = 2.5;
public static final double DEFAULT_MINIMUM_BALANCE = 500.0;
```

---

## 9. **Consistent Error Message Format**

**What it means:** All error messages follow the same pattern for better UX.

**Standardized Format:**
- `Error: Account not found. Please check the account number.`
- `Error: Invalid amount. Amount must be greater than 0.`
- `Transaction Failed: Insufficient funds. Current balance: $X,XXX.XX`

**Implementation:**
- All exceptions use standardized messages
- `displayError()` method ensures consistent "Error: " prefix
- Format helpers like `ValidationUtils.formatAmount()` ensure consistent currency display

---

## Summary

The refactoring applied these clean code principles to make the codebase:
- **More Maintainable:** Changes are localized to specific classes
- **More Testable:** Small, focused methods are easier to unit test
- **More Readable:** Clear names and documentation
- **More Extensible:** New features can be added without breaking existing code
- **More Robust:** Proper exception handling prevents runtime errors

Each principle works together to create a codebase that is professional, maintainable, and follows industry best practices.

