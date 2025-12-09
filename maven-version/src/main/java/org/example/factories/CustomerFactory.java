package org.example.factories;

import org.example.models.Customer;
import org.example.models.PremiumCustomer;
import org.example.models.RegularCustomer;

/**
 * Factory class for creating Customer instances.
 * Follows Factory Pattern to support Open/Closed Principle.
 * New customer types can be added without modifying existing code.
 */
public class CustomerFactory {
    
    /**
     * Creates a customer based on the specified type.
     *
     * @param customerType the type of customer (1 = Regular, 2 = Premium)
     * @param name         the customer's name
     * @param age          the customer's age
     * @param contact      the customer's contact number
     * @param address      the customer's address
     * @return the created customer instance
     * @throws IllegalArgumentException if customerType is invalid
     */
    public static Customer createCustomer(int customerType, String name, int age, String contact, String address) {
        return switch (customerType) {
            case 1 -> new RegularCustomer(name, age, contact, address);
            case 2 -> new PremiumCustomer(name, age, contact, address);
            default -> throw new IllegalArgumentException("Invalid customer type: " + customerType);
        };
    }
    
    /**
     * Gets the available customer types for display.
     *
     * @return array of customer type descriptions
     */
    public static String[] getCustomerTypeDescriptions() {
        return new String[]{
            "Regular Customer (Standard banking services)",
            "Premium Customer (Enhanced benefits, min balance $10,000)"
        };
    }
}

