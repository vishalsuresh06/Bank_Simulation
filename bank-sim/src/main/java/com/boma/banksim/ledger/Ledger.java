package com.boma.banksim.ledger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.boma.banksim.transaction.Transaction;

public class Ledger {
    private final List<Transaction> transactions;

    public Ledger() {
        this.transactions = new ArrayList<>();
    }

    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null.");
        }
        transactions.add(transaction);
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public int getTransactionCount() {
        return transactions.size();
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ledger{\n");
        sb.append("  transactionCount=").append(transactions.size()).append("\n");

        for (Transaction t : transactions) {
            sb.append("  ").append(t).append("\n");
        }

        sb.append("}");
        return sb.toString();
    }
}
