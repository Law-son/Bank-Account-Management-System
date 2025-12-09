# Setting Up IntelliJ IDEA to Run Tests from Main Application

## Step-by-Step Instructions

### Step 1: Build Test Classes First
1. Open the Maven tool window (View → Tool Windows → Maven)
2. Expand your project → Lifecycle
3. Double-click on **`test-compile`** to compile test classes
4. Wait for "BUILD SUCCESS"

### Step 2: Create/Edit Run Configuration

**If you DON'T see a Main configuration:**

1. Open `src/main/java/org/example/Main.java`
2. Find the `main` method (line 39)
3. Click the green play button (▶) next to `main()` OR right-click → "Run 'Main.main()'"
4. This automatically creates a run configuration named "Main"

**Now edit the configuration:**

1. Go to: **Run** → **Edit Configurations...** (or click the dropdown next to run button → Edit Configurations)
2. You should see "Main" in the list - select it
3. In the configuration panel:
   - **Main class:** `org.example.Main`
   - **Use classpath of module:** Select your module (e.g., `maven-version`)
   
4. **IMPORTANT - Add Test Classes to Classpath:**
   - Look for "Environment variables" or "Classpath" section
   - Click the **"..."** button next to "Use classpath of module"
   - OR go to the bottom and click **"Modify options"**
   - Select: **"Add dependencies with 'test' scope"** or **"Include test output"**
   - If those options don't exist:
     - Click the folder icon to manually add
     - Navigate to: `target/test-classes` folder
     - Click OK

5. Click **Apply** then **OK**

### Step 3: Verify Test Classes are Compiled

Check that `target/test-classes` folder exists and contains:
- `org/example/models/AccountTest.class`
- `org/example/services/TransactionManagerTest.class`
- etc.

If these don't exist, run `test-compile` from Maven again.

### Alternative: Use Maven Exec Plugin

If IDE configuration is difficult, you can run via Maven:

1. Open Terminal in IntelliJ (View → Tool Windows → Terminal)
2. Run:
   ```bash
   mvn clean compile test-compile exec:java -Dexec.mainClass="org.example.Main" -Dexec.classpathScope=test
   ```

This will automatically include test classes on the classpath.

