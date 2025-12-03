package main.java.bankapp.utils;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Scanner;

public class InputValidator {
    private static Scanner scanner = new Scanner(System.in);
    private static final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

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

            // Regex allows letters (any case), spaces, and single hyphens between letters
            if (input.matches("[A-Za-z]+([ '-][A-Za-z]+)*")) {
                return input;
            } else {
                System.out.println("Invalid input. Please use letters only. Hyphens and spaces are allowed (e.g., Anne-Marie).");
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
     */
    public static String getContactNumber(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String input = scanner.nextLine().trim();
            
            // Validate format: +1-XXX-XXXX
            // Pattern: starts with +1-, followed by 3 digits, dash, 4 digits
            if (input.matches("\\+1-\\d{3}-\\d{4}")) {
                return input;
            } else {
                System.out.println("Invalid contact number format. Please enter in format: +1-XXX-XXXX (e.g., +1-555-1234)");
            }
        }
    }
}