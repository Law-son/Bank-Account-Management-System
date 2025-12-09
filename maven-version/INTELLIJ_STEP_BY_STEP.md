# Step-by-Step: Running Tests from Main Application in IntelliJ IDEA

## Step 1: Compile Test Classes Using IntelliJ's Maven Tool Window

1. **Open Maven Tool Window:**
   - Click **View** → **Tool Windows** → **Maven**
   - OR use shortcut: `Alt + 4` (Windows/Linux) or `Cmd + 4` (Mac)

2. **Compile Test Classes:**
   - In the Maven tool window, expand your project name (`maven-version`)
   - Expand **Lifecycle**
   - Double-click **`test-compile`**
   - Wait for it to finish (you'll see "BUILD SUCCESS" in the Run window at the bottom)

3. **Verify Test Classes Were Created:**
   - In the Project tool window (left sidebar), expand: `target` → `test-classes`
   - You should see folders like: `org/example/models/AccountTest.class`
   - If you don't see `test-classes` folder, the compilation failed

## Step 2: Create Run Configuration

1. **Open Main.java:**
   - Navigate to `src/main/java/org/example/Main.java`

2. **Create Run Configuration:**
   - Find the `main` method (around line 39)
   - Right-click on `main` method → **Run 'Main.main()'**
   - OR click the green play button (▶) next to the line number
   - This creates a run configuration automatically

3. **Edit the Run Configuration:**
   - Click the dropdown next to the run button (top right)
   - Select **Edit Configurations...**
   - OR go to: **Run** → **Edit Configurations...**

4. **Configure to Include Test Classes:**
   - Select **Main** from the list on the left
   - On the right side:
     - **Name:** Main
     - **Main class:** `org.example.Main`
     - **Use classpath of module:** Select `maven-version`
   
   - **IMPORTANT:** At the bottom, click **"Modify options"** dropdown
   - Check: **"Add dependencies with 'test' scope"** or **"Include test output"**
   - If you don't see these options:
     - Click **"Modify options"** → **"Add VM options"**
     - In the VM options field, add:
       ```
       -cp "target/classes;target/test-classes"
       ```
       (Use semicolon `;` on Windows, colon `:` on Mac/Linux)

5. Click **Apply** → **OK**

## Step 3: Run the Application

1. Click the green play button or press `Shift + F10`
2. Navigate through menus to option **4. Run Tests**
3. You should see test results!

## Alternative: Use IntelliJ's Built-in Maven

If the above doesn't work, IntelliJ usually bundles Maven:

1. Go to: **File** → **Settings** → **Build, Execution, Deployment** → **Build Tools** → **Maven**
2. Check **"Use plugin registry"** and note the Maven home path
3. You can use this Maven in Terminal:
   - The path is usually something like: `C:\Users\<your-user>\.m2\wrapper\dists\...`
   - Or use IntelliJ's Terminal which might have Maven in PATH

## Troubleshooting

**If test classes still aren't found:**
1. Make sure `target/test-classes` folder exists and contains `.class` files
2. In Run Configuration, manually add the test-classes folder:
   - **Run** → **Edit Configurations...**
   - Select **Main**
   - Click **"..."** next to "Use classpath of module"
   - Add: `target/test-classes` folder
   - Click **OK** → **Apply** → **OK**

