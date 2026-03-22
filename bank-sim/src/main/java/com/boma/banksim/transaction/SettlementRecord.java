package com.boma.banksim.transaction;

import java.time.LocalDate;

/**
 * Records the outcome of a completed (or failed) transfer settlement.
 */
public class SettlementRecord {

    public enum Status { SETTLED, FAILED, REVERSED }

    private final String settlementId;
    private final String transactionId;
    private final LocalDate settlementDate;
    private final Status status;
    private final double amount;
    private final String notes;

    public SettlementRecord(String settlementId, String transactionId,
                            LocalDate settlementDate, Status status,
                            double amount, String notes) {
        if (settlementId == null || settlementId.isBlank()) throw new IllegalArgumentException("Settlement ID cannot be blank.");
        if (transactionId == null || transactionId.isBlank()) throw new IllegalArgumentException("Transaction ID cannot be blank.");
        if (settlementDate == null) throw new IllegalArgumentException("Settlement date cannot be null.");
        if (status == null) throw new IllegalArgumentException("Status cannot be null.");
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");

        this.settlementId = settlementId;
        this.transactionId = transactionId;
        this.settlementDate = settlementDate;
        this.status = status;
        this.amount = amount;
        this.notes = (notes == null) ? "" : notes;
    }

    public boolean isSettled() { return status == Status.SETTLED; }
    public boolean isFailed() { return status == Status.FAILED; }

    public String getSettlementId() { return settlementId; }
    public String getTransactionId() { return transactionId; }
    public LocalDate getSettlementDate() { return settlementDate; }
    public Status getStatus() { return status; }
    public double getAmount() { return amount; }
    public String getNotes() { return notes; }

    @Override
    public String toString() {
        return "SettlementRecord{id='" + settlementId + "', txId='" + transactionId +
               "', status=" + status + ", amount=" + amount + ", date=" + settlementDate + '}';
    }
}
