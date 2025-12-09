package org.example.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Utility class for running JUnit tests via Maven and displaying results in a formatted manner.
 */
public class TestRunner {
    private static final List<TestResult> testResults = new ArrayList<>();

    /**
     * Runs all JUnit tests using Maven and displays results in formatted manner.
     */
    public static void runAllTests() {
        System.out.println("\nRunning tests with JUnit...\n");
        
        // Reset results
        testResults.clear();

        try {
            // Determine OS and Maven command
            String os = System.getProperty("os.name").toLowerCase();
            boolean isWindows = os.contains("win");
            
            // Try different Maven commands
            String[] mavenCommands = isWindows 
                ? new String[]{"mvn.cmd", "mvn.bat", "mvn"} 
                : new String[]{"mvn"};
            
            ProcessBuilder processBuilder = null;
            String mavenCmd = null;
            
            // Find which Maven command works
            for (String cmd : mavenCommands) {
                try {
                    Process testProcess = new ProcessBuilder(cmd, "--version").start();
                    testProcess.waitFor();
                    if (testProcess.exitValue() == 0) {
                        mavenCmd = cmd;
                        break;
                    }
                } catch (Exception ignored) {
                    // Try next command
                }
            }
            
            if (mavenCmd == null) {
                System.out.println("Error: Maven not found. Please install Maven or ensure it's in your PATH.");
                System.out.println("To run tests manually, execute: mvn test");
                System.out.println();
                return;
            }
            
            // Run Maven test with verbose output to capture test names
            processBuilder = new ProcessBuilder(mavenCmd, "test");
            processBuilder.redirectErrorStream(true);
            
            // Set working directory to project root
            String userDir = System.getProperty("user.dir");
            processBuilder.directory(new java.io.File(userDir));
            
            Process process = processBuilder.start();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            
            String line;
            String currentTestClass = null;
            while ((line = reader.readLine()) != null) {
                // Parse test results from Maven Surefire output
                parseTestResult(line);
            }
            
            int exitCode = process.waitFor();
            
            // Always parse XML reports - they're more reliable than console output
            parseFromSurefireReports();
            
            // Display results
            displayTestResults();
            displaySummary(exitCode == 0);
            
        } catch (Exception e) {
            System.out.println("Error running tests: " + e.getMessage());
            e.printStackTrace();
            System.out.println();
            System.out.println("Please ensure Maven is installed and accessible.");
            System.out.println("To run tests manually, execute: mvn test");
            System.out.println();
        }
    }

    /**
     * Parses Maven Surefire test output to extract test names and results.
     * Maven Surefire output typically shows test execution, but we'll rely more on XML reports.
     */
    private static void parseTestResult(String line) {
        // Skip summary lines that contain words like "Tests", "skip", "run", "Failures"
        String lowerLine = line.toLowerCase();
        if (lowerLine.contains("tests run:") || 
            lowerLine.contains("failures:") || 
            lowerLine.contains("errors:") ||
            lowerLine.contains("skipped:") ||
            lowerLine.matches(".*\\d+.*test.*run.*")) {
            return; // Skip summary lines
        }
        
        // Pattern to match actual test method execution: "org.example.models.AccountTest.depositUpdatesBalance()"
        // Or just method name followed by parentheses: "depositUpdatesBalance()"
        Pattern testMethodPattern = Pattern.compile(
            "(?:org\\.example\\.[\\w.]+)\\.([a-z][a-zA-Z0-9]+)\\(\\)|" +  // Full qualified: ClassName.methodName()
            "([a-z][a-zA-Z0-9]+)\\(\\)(?=\\s|$)",  // Just methodName() at end of line or before space
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher testMatcher = testMethodPattern.matcher(line);
        
        if (testMatcher.find()) {
            String testName = testMatcher.group(1) != null ? testMatcher.group(1) : testMatcher.group(2);
            
            // Filter out common false positives
            if (testName != null && 
                !testName.equalsIgnoreCase("skip") && 
                !testName.equalsIgnoreCase("tests") &&
                !testName.equalsIgnoreCase("run") &&
                !testName.equalsIgnoreCase("failures") &&
                !testName.equalsIgnoreCase("errors") &&
                testName.length() > 3) {
                
                boolean passed = !lowerLine.contains("fail") && 
                                !lowerLine.contains("error") &&
                                !lowerLine.contains("✗");
                
                // Only add if not already present
                if (testResults.stream().noneMatch(r -> r.testName.equals(testName))) {
                    testResults.add(new TestResult(testName, passed));
                }
            }
        }
    }
    
    /**
     * Parses test results from Maven Surefire XML reports (more reliable than console parsing).
     */
    private static void parseFromSurefireReports() {
        try {
            Path surefireReportsDir = Paths.get("target", "surefire-reports");
            if (!Files.exists(surefireReportsDir)) {
                return;
            }
            
            // Always parse XML reports as they're more reliable - clear existing results first if they're wrong
            // (Check if we have suspicious results like "skip" or "Tests")
            boolean hasBadResults = testResults.stream()
                .anyMatch(r -> r.testName.equalsIgnoreCase("skip") || 
                              r.testName.equalsIgnoreCase("tests") ||
                              r.testName.length() < 4);
            
            if (hasBadResults) {
                testResults.clear();
            }
            
            // Parse XML reports - these contain structured test data
            try (Stream<Path> paths = Files.walk(surefireReportsDir)) {
                paths.filter(path -> Files.isRegularFile(path) && 
                             path.toString().endsWith(".xml"))
                     .forEach(TestRunner::parseXmlReport);
            }
            
            // If still no results, try parsing from TXT files
            if (testResults.isEmpty()) {
                try (Stream<Path> paths = Files.walk(surefireReportsDir)) {
                    paths.filter(path -> Files.isRegularFile(path) && 
                                 path.toString().endsWith(".txt"))
                         .forEach(TestRunner::parseTxtReport);
                }
            }
            
        } catch (Exception e) {
            // Silently fail - console parsing is preferred
        }
    }
    
    /**
     * Parses XML report files from Surefire - these contain structured test data.
     * Extracts individual test method names from testcase elements.
     */
    private static void parseXmlReport(Path file) {
        try {
            String content = Files.readString(file);
            
            // Find all testcase elements
            // Format: <testcase name="methodName" classname="org.example.ClassName" ...>
            // We want the 'name' attribute value, not the 'classname'
            Pattern testCasePattern = Pattern.compile(
                "<testcase[^>]*>",
                Pattern.CASE_INSENSITIVE
            );
            
            Matcher testCaseMatcher = testCasePattern.matcher(content);
            while (testCaseMatcher.find()) {
                String testCaseTag = testCaseMatcher.group();
                
                // Extract the 'name' attribute value specifically
                Pattern namePattern = Pattern.compile("name\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
                Matcher nameMatcher = namePattern.matcher(testCaseTag);
                
                if (nameMatcher.find()) {
                    String testMethodName = nameMatcher.group(1);
                    
                    // Skip if it looks like a class name (contains dots or starts with uppercase package-style)
                    if (testMethodName != null && 
                        testMethodName.length() >= 4 &&
                        !testMethodName.contains(".") &&  // Method names don't have dots
                        !testMethodName.equals("skip") &&
                        !testMethodName.equals("tests")) {
                        
                        // Check for failure/error/skipped in the testcase element
                        int testCaseStart = testCaseMatcher.start();
                        int testCaseEnd = content.indexOf("</testcase>", testCaseStart);
                        if (testCaseEnd == -1) {
                            testCaseEnd = content.indexOf("/>", testCaseStart);
                            if (testCaseEnd != -1) {
                                testCaseEnd += 2; // Include the />
                            }
                        } else {
                            testCaseEnd += 11; // Include </testcase>
                        }
                        
                        boolean passed = true;
                        if (testCaseEnd != -1 && testCaseEnd < content.length()) {
                            String testCaseElement = content.substring(testCaseStart, testCaseEnd);
                            passed = !testCaseElement.contains("<failure") && 
                                    !testCaseElement.contains("<error") &&
                                    !testCaseElement.contains("<skipped");
                        }
                        
                        // Only add if not already present
                        if (testResults.stream().noneMatch(r -> r.testName.equals(testMethodName))) {
                            testResults.add(new TestResult(testMethodName, passed));
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
    }
    
    /**
     * Parses a .txt report file from Surefire.
     */
    private static void parseTxtReport(Path file) {
        try {
            List<String> lines = Files.readAllLines(file);
            for (String line : lines) {
                // Look for test method names in format: "testMethod()"
                Pattern pattern = Pattern.compile("(\\w+)\\(\\)");
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    String testName = matcher.group(1);
                    if (!testName.equals("toString") && !testName.equals("equals") && 
                        !testName.equals("hashCode") && testName.length() > 3) {
                        boolean passed = !line.toLowerCase().contains("fail") && 
                                        !line.toLowerCase().contains("error");
                        if (testResults.stream().noneMatch(r -> r.testName.equals(testName))) {
                            testResults.add(new TestResult(testName, passed));
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
    }
    
    /**
     * Parses any report file to extract test names.
     */
    private static void parseReportFile(Path file) {
        try {
            String content = Files.readString(file);
            // Look for test method patterns
            Pattern pattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"|(\\w+)\\(\\)");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String testName = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                if (testName != null && !testName.equals("toString") && 
                    testResults.stream().noneMatch(r -> r.testName.equals(testName))) {
                    boolean passed = !content.toLowerCase().contains("\"" + testName + "\".*fail");
                    testResults.add(new TestResult(testName, passed));
                }
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
    }

    /**
     * Displays individual test results in the specified format.
     */
    private static void displayTestResults() {
        if (testResults.isEmpty()) {
            // If we couldn't parse individual tests, show a message
            System.out.println("Running Maven tests...");
            System.out.println("(Individual test results will be shown if available)");
            return;
        }
        
        for (TestResult result : testResults) {
            String status = result.passed ? "PASSED" : "FAILED";
            int dotCount = Math.max(0, 50 - result.testName.length());
            String dots = ".".repeat(dotCount);
            System.out.printf("Test: %s %s %s%n", result.testName, dots, status);
        }
    }

    /**
     * Displays the test summary.
     */
    private static void displaySummary(boolean allPassed) {
        System.out.println();
        
        if (testResults.isEmpty()) {
            if (allPassed) {
                System.out.println("All tests passed successfully!");
            } else {
                System.out.println("Some tests failed. Check Maven output above for details.");
            }
        } else {
            long passedCount = testResults.stream().filter(r -> r.passed).count();
            long failedCount = testResults.stream().filter(r -> !r.passed).count();
            int totalCount = testResults.size();
            
            if (failedCount == 0) {
                System.out.printf("All %d tests passed successfully!%n", totalCount);
            } else {
                System.out.printf("Tests completed: %d passed, %d failed out of %d total tests.%n",
                        passedCount, failedCount, totalCount);
            }
        }
        System.out.println();
    }

    /**
     * Inner class to store test result information.
     */
    private static class TestResult {
        String testName;
        boolean passed;

        TestResult(String testName, boolean passed) {
            this.testName = testName;
            this.passed = passed;
        }
    }
}

