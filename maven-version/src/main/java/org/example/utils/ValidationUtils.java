package org.example.utils;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * Utility class for input validation and formatting.
 * Provides centralized validation logic using regex patterns and Predicate lambdas.
 */
public class ValidationUtils {
    private static Scanner scanner = new Scanner(System.in);
    private static final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    
    // Regex patterns for validation
    /** Pattern for account numbers: ACC followed by exactly 3 digits (e.g., ACC001, ACC123) */
    public static final String ACCOUNT_NUMBER_PATTERN = "^ACC\\d{3}$";
    
    /** Pattern for phone numbers: +1-XXX-XXXX format (e.g., +1-555-1234) */
    public static final String PHONE_NUMBER_PATTERN = "^\\+1-\\d{3}-\\d{4}$";
    
    /** Pattern for names: letters, spaces, hyphens, and apostrophes allowed (e.g., John Doe, Anne-Marie, O'Brien) */
    public static final String NAME_PATTERN = "^[A-Za-z]+([ '-][A-Za-z]+)*$";
    
    /** Pattern for addresses: alphanumeric, spaces, commas, periods, hyphens, and common address symbols */
    public static final String ADDRESS_PATTERN = "^[A-Za-z0-9\\s,\\.\\-#/]+$";

    public static String getString(String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine().trim();
    }

    /**
     * Gets name from the user while excluding numbers and symbols, except '-'.
     * Continues prompting until a valid integer within the range is entered.
     */
    public static String getName(String prompt) {
        String input;
        while (true) {
            System.out.print(prompt + ": ");
            input = scanner.nextLine().trim();

            // Use centralized NAME_PATTERN regex
            if (input.matches(NAME_PATTERN)) {
                return input;
            } else {
                System.out.println("Invalid input. Please use letters only. Hyphens, spaces, and apostrophes are allowed (e.g., Anne-Marie, O'Brien).");
            }
        }
    }

    /**
     * Gets an integer within a specified range.
     * Continues prompting until a valid integer within the range is entered.
     */
    public static int getIntInRange(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt + ": ");
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.printf("Invalid option. Please enter a number between %d and %d.%n", min, max);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer. Please enter a valid number.");
            }
        }
    }

    /**
     * Gets a positive double value.
     * Continues prompting until a valid positive number is entered.
     * Accepts formats like: 2694, 2,694, $2,694, etc.
     */
    public static double getDoublePositive(String prompt) {
        while (true) {
            try {
                System.out.print(prompt + ": ");
                String input = scanner.nextLine();
                double value = parseAmount(input);
                if (value > 0) {
                    return value;
                } else {
                    System.out.println("Invalid amount. Amount must be greater than 0. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid amount (e.g., 2694, 2,694, or $2,694).");
            }
        }
    }

    /**
     * Gets a double value that is at least a minimum value.
     * Continues prompting until a valid number >= min is entered.
     * Accepts formats like: 2694, 2,694, $2,694, etc.
     */
    public static double getDoubleMin(String prompt, double min) {
        while (true) {
            try {
                System.out.print(prompt + ": ");
                String input = scanner.nextLine();
                double value = parseAmount(input);
                if (value >= min) {
                    return value;
                } else {
                    System.out.printf("Invalid amount. Minimum amount is %s. Please try again.%n", formatAmount(min));
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid amount (e.g., 2694, 2,694, or $2,694).");
            }
        }
    }

    /**
     * Gets a yes/no confirmation (Y or N).
     * Continues prompting until a valid Y or N (case-insensitive) is entered.
     */
    public static String getYesNo(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("Y") || input.equalsIgnoreCase("N")) {
                return input;
            } else {
                System.out.println("Invalid input. Please enter Y or N.");
            }
        }
    }

    /**
     * Gets a positive integer value.
     * Continues prompting until a valid positive integer is entered.
     */
    public static int getIntPositive(String prompt) {
        while (true) {
            try {
                System.out.print(prompt + ": ");
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value > 0) {
                    return value;
                } else {
                    System.out.println("Invalid input. Please enter a positive number.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer. Please enter a valid number.");
            }
        }
    }

    /**
     * Parses an amount string that may contain dollar signs, commas, or be a plain number.
     * Examples: "2694", "2,694", "$2,694", "$2694" all work.
     */
    public static double parseAmount(String input) throws NumberFormatException {
        if (input == null || input.trim().isEmpty()) {
            throw new NumberFormatException("Empty input");
        }
        
        // Remove dollar signs, spaces, and commas
        String cleaned = input.trim()
                .replace("$", "")
                .replace(",", "")
                .replace(" ", "");
        
        return Double.parseDouble(cleaned);
    }

    /**
     * Formats a double amount as currency with dollar sign and comma separators.
     * Example: 2694.5 -> "$2,694.50"
     */
    public static String formatAmount(double amount) {
        return currencyFormatter.format(amount);
    }

    /**
     * Gets a contact number in the format "+1-XXX-XXXX".
     * Continues prompting until a valid contact number is entered.
     * Uses regex pattern validation.
     */
    public static String getContactNumber(String prompt) {
        return validateInputWithPattern(
                prompt,
                PHONE_NUMBER_PATTERN,
                "Invalid contact number format. Please enter in format: +1-XXX-XXXX (e.g., +1-555-1234)"
        );
    }
    
    /**
     * Gets an account number in the format "ACC###" (e.g., ACC001, ACC123).
     * Continues prompting until a valid account number format is entered.
     * Uses regex pattern validation.
     *
     * @param prompt the prompt message to display
     * @param allowZero if true, allows "0" as a special value to go back
     * @return the validated account number, or "0" if allowZero is true and user entered "0"
     */
    public static String getAccountNumber(String prompt, boolean allowZero) {
        while (true) {
            System.out.print(prompt + ": ");
            String input = scanner.nextLine().trim();
            
            // Allow "0" as special value to go back if enabled
            if (allowZero && "0".equals(input)) {
                return input;
            }
            
            // Validate format: ACC followed by exactly 3 digits
            if (input.matches(ACCOUNT_NUMBER_PATTERN)) {
                return input;
            } else {
                System.out.println("Invalid account number format. Please enter in format: ACC### (e.g., ACC001, ACC123)");
            }
        }
    }
    
    /**
     * Gets an account number without allowing "0" as a special value.
     *
     * @param prompt the prompt message to display
     * @return the validated account number
     */
    public static String getAccountNumber(String prompt) {
        return getAccountNumber(prompt, false);
    }
    
    /**
     * Gets an address from the user.
     * Continues prompting until a valid address format is entered.
     * Uses regex pattern validation.
     *
     * @param prompt the prompt message to display
     * @return the validated address
     */
    public static String getAddress(String prompt) {
        return validateInputWithPattern(
                prompt,
                ADDRESS_PATTERN,
                "Invalid address format. Please enter a valid address (alphanumeric, spaces, commas, periods, hyphens allowed)"
        );
    }
    
    /**
     * Generic validation method using Predicate lambdas for dynamic validation rules.
     * Continues prompting until input passes the validation predicate.
     *
     * @param prompt the prompt message to display
     * @param validator the Predicate function that returns true if input is valid
     * @param errorMessage the error message to display when validation fails
     * @return the validated input string
     */
    public static String validateInput(String prompt, Predicate<String> validator, String errorMessage) {
        while (true) {
            System.out.print(prompt + ": ");
            String input = scanner.nextLine().trim();
            
            if (validator.test(input)) {
                return input;
            } else {
                System.out.println(errorMessage);
            }
        }
    }
    
    /**
     * Validates input against a regex pattern.
     * Continues prompting until input matches the pattern.
     *
     * @param prompt the prompt message to display
     * @param pattern the regex pattern to match
     * @param errorMessage the error message to display when validation fails
     * @return the validated input string
     */
    public static String validateInputWithPattern(String prompt, String pattern, String errorMessage) {
        return validateInput(prompt, input -> input.matches(pattern), errorMessage);
    }
    
    /**
     * Validates if an account number has the correct format.
     * Can be used as a standalone validation check.
     *
     * @param accountNumber the account number to validate
     * @return true if the account number matches the ACC\d{3} pattern, false otherwise
     */
    public static boolean isValidAccountNumber(String accountNumber) {
        return accountNumber != null && accountNumber.matches(ACCOUNT_NUMBER_PATTERN);
    }
    
    /**
     * Validates if a phone number has the correct format.
     * Can be used as a standalone validation check.
     *
     * @param phoneNumber the phone number to validate
     * @return true if the phone number matches the +1-XXX-XXXX pattern, false otherwise
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches(PHONE_NUMBER_PATTERN);
    }
    
    /**
     * Validates if an address has the correct format.
     * Can be used as a standalone validation check.
     *
     * @param address the address to validate
     * @return true if the address matches the valid address pattern, false otherwise
     */
    public static boolean isValidAddress(String address) {
        return address != null && !address.trim().isEmpty() && address.matches(ADDRESS_PATTERN);
    }
}

