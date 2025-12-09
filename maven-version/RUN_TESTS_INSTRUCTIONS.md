# How to Run Tests from Main Application

## Problem
Test classes are not on the runtime classpath when running the main application from IDE.

## Solution Options

### Option 1: Create IntelliJ Run Configuration with Test Classes

1. **Create a new Run Configuration:**
   - Click on `Main.java` in the project
   - Right-click → "Run 'Main.main()'" OR click the green play button next to `main()`
   - This creates a run configuration automatically

2. **Edit the Run Configuration:**
   - Go to: **Run** → **Edit Configurations...**
   - Find your "Main" configuration (it should be listed)
   - Under "Use classpath of module:", select your module
   - Click on "Modify options" (at the bottom left)
   - Check: **"Include dependencies with 'Provided' scope"** (if available)
   - OR manually add test output:
     - Go to "Classpath" tab (if visible)
     - Click "+" to add
     - Select "target/test-classes" folder

3. **Alternative: Use JUnit Run Configuration**
   - Create a new JUnit run configuration
   - Test kind: "All in package"
   - Package: `org.example`
   - This will run tests directly

### Option 2: Use Maven to Run (Recommended)

If you have Maven installed:

```bash
# Compile everything including tests
mvn clean compile test-compile

# Run with test classes on classpath
mvn exec:java -Dexec.mainClass="org.example.Main" -Dexec.classpathScope=test
```

### Option 3: Build and Run from Command Line

```bash
# Windows (PowerShell)
.\mvnw.cmd clean compile test-compile exec:java -Dexec.mainClass="org.example.Main" -Dexec.classpathScope=test

# Mac/Linux
./mvnw clean compile test-compile exec:java -Dexec.mainClass="org.example.Main" -Dexec.classpathScope=test
```

### Option 4: Quick Fix - Run Tests Directly

Instead of running tests from Main menu, you can:
- Right-click on test folder → "Run All Tests"
- Or run individual test classes
- This uses JUnit's native test runner

