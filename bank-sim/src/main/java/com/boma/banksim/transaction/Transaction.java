package com.boma.banksim.transaction;

import java.time.LocalDateTime;

public class Transaction {
    private final String transactionId;
    private final TransactionType type;
    private final double amount;
    private final LocalDateTime timestamp;
    private final String sourceAccountId;
    private final String destinationAccountId;
    private final String description;

    public Transaction(
            String transactionId,
            TransactionType type,
            double amount,
            LocalDateTime timestamp,
            String sourceAccountId,
            String destinationAccountId,
            String description
    ) {
        checkNotBlank(transactionId, "Transaction ID");
        checkNotNull(type, "Transaction type");
        checkNotNull(timestamp, "Timestamp");
        checkPositive(amount, "Amount");

        if (sourceAccountId == null && destinationAccountId == null) {
            throw new IllegalArgumentException(
                    "A transaction must involve at least one account."
            );
        }

        if (sourceAccountId != null) {
            checkNotBlank(sourceAccountId, "Source account ID");
        }

        if (destinationAccountId != null) {
            checkNotBlank(destinationAccountId, "Destination account ID");
        }

        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.description = (description == null) ? "" : description;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getSourceAccountId() {
        return sourceAccountId;
    }

    public String getDestinationAccountId() {
        return destinationAccountId;
    }

    public String getDescription() {
        return description;
    }

    private void checkNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null.");
        }
    }

    private void checkNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank.");
        }
    }

    private void checkPositive(double value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", type=" + type +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                ", sourceAccountId='" + sourceAccountId + '\'' +
                ", destinationAccountId='" + destinationAccountId + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}