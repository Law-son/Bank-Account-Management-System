# Git Workflow Documentation

## Overview
This document outlines the complete Git workflow used in the Bank Account Management System project, from initial setup through refactoring and testing implementation.

## Project Structure Evolution

### Initial Setup
1. **Started with main repository**
   - Original project was set up using IntelliJ IDEA
   - Basic project structure was established

2. **Migration to Maven**
   - Decided to migrate from IntelliJ project structure to Maven for better dependency management and build automation
   - Set up the project again with Maven instead of IntelliJ

### Branch Strategy

#### 1. **maven-version Branch**
- **Purpose**: Migrate project from IntelliJ project structure to Maven-based project
- **Created**: After deciding to set up the project with Maven
- **Changes**:
  - Created `pom.xml` for Maven dependency management
  - Restructured project to follow Maven standard directory layout (`src/main/java`, `src/test/java`)
  - Migrated all source code to new Maven structure
  - Configured Maven build plugins and dependencies
- **Workflow**:
  ```bash
  git checkout -b maven-version
  # ... made Maven migration changes ...
  git add .
  git commit -m "Migrate project to Maven structure"
  git push origin maven-version
  ```
- **Merge to main**:
  ```bash
  git checkout main
  git merge maven-version
  git push origin main
  ```

#### 2. **feature/refactor Branch**
- **Purpose**: Refactor codebase to follow clean code principles and improved architecture
- **Created**: After maven-version was merged to main, created this branch to hold refactored code
- **Changes**:
  - Reorganized package structure:
    - Flattened model classes to `models/` (Account, SavingsAccount, CheckingAccount, Customer, etc.)
    - Created `models/exceptions/` for custom exception handling
    - Moved services to `services/` package (AccountManager, TransactionManager, StatementGenerator)
    - Renamed `InputValidator` to `ValidationUtils` in `utils/` package
  - Implemented custom exceptions:
    - `InvalidAmountException`
    - `InsufficientFundsException`
    - `OverdraftExceededException`
    - `AccountNotFoundException`
  - Applied clean code principles:
    - Single Responsibility Principle (created TransferService)
    - DRY (extracted common methods)
    - Consistent naming conventions
    - Added comprehensive JavaDoc documentation
  - Enhanced error handling with standardized error messages
  - Added transfer functionality between accounts
- **Workflow**:
  ```bash
  git checkout -b feature/refactor
  # ... made refactoring changes ...
  git add .
  git commit -m "Refactor codebase: reorganize packages, implement exceptions, apply clean code principles"
  git push origin feature/refactor
  ```
- **Merge to main**:
  ```bash
  git checkout main
  git merge feature/refactor
  git push origin main
  ```

#### 3. **feature/testing Branch**
- **Purpose**: Implement comprehensive JUnit testing suite
- **Created**: After feature/refactor was merged to main, created this branch to contain testing codes and results
- **Changes**:
  - Added JUnit 5 dependencies to `pom.xml`
  - Created test classes:
    - `AccountTest.java` - Tests for deposit and withdraw methods
    - `TransactionManagerTest.java` - Tests for transaction recording
    - `TransferServiceTest.java` - Tests for transfer functionality
    - `ExceptionTest.java` - Tests for exception handling
  - Created `TestRunner.java` utility class for running tests from main application
  - Integrated test execution into main menu (option 4: Run Tests)
  - Configured Maven Surefire plugin for test execution
  - Added test execution via ProcessBuilder to run `mvn test`
- **Workflow**:
  ```bash
  git checkout -b feature/testing
  # ... implemented test suite ...
  git add .
  git commit -m "Add comprehensive JUnit test suite and test runner integration"
  git push origin feature/testing
  ```
- **Merge to main**:
  ```bash
  git checkout main
  git merge feature/testing
  git push origin main
  ```

## Chronological Workflow Summary

### Step-by-Step Git Workflow

1. **Initial State**
   - Started with main repository containing IntelliJ-based project

2. **Create maven-version Branch**
   ```bash
   git checkout main
   git checkout -b maven-version
   # Set up project with Maven instead of IntelliJ
   # Created pom.xml, restructured directories
   git add .
   git commit -m "Migrate project to Maven structure"
   git push origin maven-version
   ```

3. **Merge maven-version to main**
   ```bash
   git checkout main
   git merge maven-version
   git push origin main
   ```

4. **Create feature/refactor Branch**
   ```bash
   git checkout main
   git checkout -b feature/refactor
   # Refactored codebase: reorganized packages, implemented exceptions, applied clean code
   git add .
   git commit -m "Refactor codebase: reorganize packages, implement exceptions, apply clean code principles"
   git push origin feature/refactor
   ```

5. **Merge feature/refactor to main**
   ```bash
   git checkout main
   git merge feature/refactor
   git push origin main
   ```

6. **Create feature/testing Branch**
   ```bash
   git checkout main
   git checkout -b feature/testing
   # Implemented comprehensive JUnit test suite and test runner
   git add .
   git commit -m "Add comprehensive JUnit test suite and test runner integration"
   git push origin feature/testing
   ```

7. **Merge feature/testing to main**
   ```bash
   git checkout main
   git merge feature/testing
   git push origin main
   ```

## Current Branch Structure

```
main (stable, production-ready)
├── maven-version (merged ✓)
├── feature/refactor (merged ✓)
└── feature/testing (merged ✓)
```

## Git Workflow Best Practices Used

1. **Feature Branch Workflow**
   - Each major feature or change gets its own branch
   - Branches are named descriptively (`feature/refactor`, `feature/testing`)
   - All changes are tested before merging to main

2. **Commit Messages**
   - Clear, descriptive commit messages
   - Each commit represents a logical unit of work
   - Messages explain what was changed and why

3. **Merge Strategy**
   - After pushing to feature branch, merge to main
   - Main branch always contains stable, tested code
   - Feature branches are kept for reference

4. **Incremental Development**
   - Work was done in logical phases:
     - Phase 1: Maven migration
     - Phase 2: Code refactoring
     - Phase 3: Testing implementation

## Summary of Changes by Branch

### maven-version
- Maven project structure
- `pom.xml` configuration
- Standard Maven directory layout

### feature/refactor
- Package reorganization
- Custom exception implementation
- Clean code principles application
- Transfer functionality
- Enhanced error handling

### feature/testing
- JUnit 5 test suite (27+ test methods)
- Test runner integration
- Maven Surefire configuration
- Test execution from application menu

## Next Steps / Future Branches

Potential future branches:
- `feature/persistence` - Add database persistence
- `feature/security` - Implement security features
- `feature/api` - REST API implementation
- `feature/ui` - Graphical user interface
- `bugfix/` - Bug fixes and patches

## Notes

- All feature branches were merged into main after completion
- Main branch represents the stable, production-ready codebase
- Each branch maintained its own focused scope of changes
- Testing was implemented as a separate feature to maintain code quality

