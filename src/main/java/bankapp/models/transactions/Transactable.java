package main.java.bankapp.models.transactions;

public interface Transactable {
    // Returns boolean to indicate success/failure
    boolean processTransaction(double amount, String type);
}