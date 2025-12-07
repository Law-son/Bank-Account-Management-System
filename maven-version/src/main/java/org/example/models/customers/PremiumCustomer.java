package org.example.models.customers;

public class PremiumCustomer extends Customer {
    private double minimumBalance = 10000;

    public PremiumCustomer(String name, int age, String contact, String address) {
        super(name, age, contact, address);
    }

    @Override
    public void displayCustomerDetails() {
        System.out.println("ID: " + getCustomerId() + " | Name: " + getName() + " | Type: Premium (Fees Waived)");
    }

    @Override
    public String getCustomerType() {
        return "Premium";
    }

    @Override
    public boolean hasWaivedFees() {
        return true;
    }
}