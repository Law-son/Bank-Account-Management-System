package org.example.models.interfaces;

public interface Transactable {
    // Returns boolean to indicate success/failure
    boolean processTransaction(double amount, String type);
}

