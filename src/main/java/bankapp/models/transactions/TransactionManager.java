package main.java.bankapp.models.transactions;

public class TransactionManager {
    private Transaction[] transactions = new Transaction[200];
    private int transactionCount = 0;

    public void addTransaction(Transaction transaction) {
        if (transactionCount < transactions.length) {
            transactions[transactionCount++] = transaction;
        } else {
            System.out.println("Transaction history full. Cannot record new log.");
        }
    }

    public void viewTransactionsByAccount(String accountNumber) {
        System.out.println("\n--- Transaction History for " + accountNumber + " ---");
        boolean found = false;

        // Display in reverse chronological order (Newest first)
        for (int i = transactionCount - 1; i >= 0; i--) {
            if (transactions[i].getAccountNumber().equals(accountNumber)) {
                transactions[i].displayTransactionDetails();
                found = true;
            }
        }

        if (!found) {
            System.out.println("No transactions found.");
        } else {
            printSummary(accountNumber);
        }
    }

    private void printSummary(String accountNumber) {
        double deposits = 0;
        double withdrawals = 0;

        for (int i = 0; i < transactionCount; i++) {
            if (transactions[i].getAccountNumber().equals(accountNumber)) {
                if (transactions[i].getType().equalsIgnoreCase("Deposit")) {
                    deposits += transactions[i].getAmount();
                } else {
                    withdrawals += transactions[i].getAmount();
                }
            }
        }
        System.out.println("Summary: Total Deposits: $" + deposits + " | Total Withdrawals: $" + withdrawals);
    }
}