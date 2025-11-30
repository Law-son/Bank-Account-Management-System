package main.java.bankapp.models.accounts;

import main.java.bankapp.models.customers.Customer;
import main.java.bankapp.utils.InputValidator;

public class CheckingAccount extends Account {
    private static final double DEFAULT_OVERDRAFT_LIMIT = 1000;
    private static final double DEFAULT_MONTHLY_FEE = 10;
    
    private double overdraftLimit = DEFAULT_OVERDRAFT_LIMIT;
    private double monthlyFee = DEFAULT_MONTHLY_FEE;

    public CheckingAccount(Customer customer, double initialDeposit) {
        super(customer, initialDeposit);
    }

    @Override
    public boolean withdraw(double amount) {
        if (balance - amount >= -overdraftLimit) {
            balance -= amount;
            return true;
        }
        System.out.println("Transaction Failed: Exceeds overdraft limit of " + InputValidator.formatAmount(overdraftLimit));
        return false;
    }

    @Override
    public void displayAccountDetails() {
        System.out.printf(" %s | %-18s | Checking   | %-15s | %s%n",
                getAccountNumber(), getCustomer().getName(), InputValidator.formatAmount(balance), getStatus());
        System.out.println("        |                    | Overdraft Limit: " + InputValidator.formatAmount(overdraftLimit) + " | Monthly Fee: " + InputValidator.formatAmount(monthlyFee));
        System.out.println("-------------------------------------------------------------------------------");
    }

    @Override
    public String getAccountType() {
        return "Checking";
    }

    public double getOverdraftLimit() { return overdraftLimit; }
    public double getMonthlyFee() { return monthlyFee; }
    
    // Static methods to get default values without creating an instance
    public static double getDefaultOverdraftLimit() { return DEFAULT_OVERDRAFT_LIMIT; }
    public static double getDefaultMonthlyFee() { return DEFAULT_MONTHLY_FEE; }

}