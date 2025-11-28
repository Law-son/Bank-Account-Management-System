package main.java.bankapp.models.accounts;

import main.java.bankapp.models.customers.Customer;

public class CheckingAccount extends Account {
    private double overdraftLimit = 1000;
    private double monthlyFee = 10;

    public CheckingAccount(Customer customer, double initialDeposit) {
        super(customer, initialDeposit);
    }

    @Override
    public boolean withdraw(double amount) {
        if (balance - amount >= -overdraftLimit) {
            balance -= amount;
            return true;
        }
        System.out.println("Transaction Failed: Exceeds overdraft limit of $" + overdraftLimit);
        return false;
    }

    @Override
    public void displayAccountDetails() {
        System.out.printf(" %s | %-18s | Checking   | $%,.2f       | Active%n",
                getAccountNumber(), getCustomer().getName(), balance);
        System.out.println("        |                    | Overdraft Limit: $1,000.00 | Monthly Fee: $10.00");
        System.out.println("-------------------------------------------------------------------------------");
    }

    @Override
    public String getAccountType() {
        return "Checking";
    }

    public void applyMonthlyFee() {
        if (!getCustomer().hasWaivedFees()) {
            balance -= monthlyFee;
        }
    }
}