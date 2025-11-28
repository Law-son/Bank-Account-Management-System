package main.java.bankapp.models.accounts;

import main.java.bankapp.models.customers.Customer;

public class SavingsAccount extends Account {
    private double interestRate = 3.5;
    private double minimumBalance = 500;

    public SavingsAccount(Customer customer, double initialDeposit) {
        super(customer, initialDeposit);
    }

    @Override
    public boolean withdraw(double amount) {
        if (balance - amount >= minimumBalance) {
            balance -= amount;
            return true;
        }
        System.out.println("Transaction Failed: Minimum balance of $" + minimumBalance + " must be maintained.");
        return false;
    }

    @Override
    public void displayAccountDetails() {
        System.out.printf(" %s | %-18s | Savings    | $%,.2f       | Active%n",
                getAccountNumber(), getCustomer().getName(), balance);
        System.out.println("        |                    | Interest Rate: 3.5% | Min Balance: $500.00");
        System.out.println("-------------------------------------------------------------------------------");
    }


    @Override
    public String getAccountType() {
        return "Savings";
    }

    public double calculateInterest() {
        return balance * (interestRate / 100);
    }
}