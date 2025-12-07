package org.example.models;

public class RegularCustomer extends Customer {

    public RegularCustomer(String name, int age, String contact, String address) {
        super(name, age, contact, address);
    }

    @Override
    public void displayCustomerDetails() {
        System.out.println("ID: " + getCustomerId() + " | Name: " + getName() + " | Type: Regular");
    }

    @Override
    public String getCustomerType() {
        return "Regular";
    }
}

