package org.example.models;

/**
 * Abstract base class representing a bank customer.
 */
public abstract class Customer {
    private String customerId;
    private String name;
    private int age;
    private String contact;
    private String address;

    // Static counter for ID generation
    private static int customerCounter = 0;

    public Customer(String name, int age, String contact, String address) {
        this.customerId = "CUST" + String.format("%03d", ++customerCounter); // Auto-generate ID
        this.name = name;
        this.age = age;
        this.contact = contact;
        this.address = address;
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getContact() { return contact; }
    public String getAddress() { return address; }

    public abstract void displayCustomerDetails();
    public abstract String getCustomerType();

    public boolean hasWaivedFees() {
        return false;
    }
}

