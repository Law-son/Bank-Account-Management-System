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
        System.out.println("ACC: " + getAccountNumber() + " | Type: Checking | Balance: $" + balance +
                " | Overdraft Limit: $" + overdraftLimit);
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