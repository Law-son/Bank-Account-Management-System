package main.java.bankapp.utils;
import java.util.Scanner;

public class InputValidator {
    private static Scanner scanner = new Scanner(System.in);

    public static String getString(String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine().trim();
    }

    public static double getDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt + ": ");
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    public static int getInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt + ": ");
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer.");
            }
        }
    }

    /**
     * Gets an integer within a specified range (inclusive).
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
     */
    public static double getDoublePositive(String prompt) {
        while (true) {
            try {
                System.out.print(prompt + ": ");
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value > 0) {
                    return value;
                } else {
                    System.out.println("Invalid amount. Amount must be greater than 0. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid amount.");
            }
        }
    }

    /**
     * Gets a double value that is at least a minimum value.
     * Continues prompting until a valid number >= min is entered.
     */
    public static double getDoubleMin(String prompt, double min) {
        while (true) {
            try {
                System.out.print(prompt + ": ");
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value >= min) {
                    return value;
                } else {
                    System.out.printf("Invalid amount. Minimum amount is $%.2f. Please try again.%n", min);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid amount.");
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
}