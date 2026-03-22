package com.boma.banksim.ledger;

import java.time.LocalDate;

/**
 * A double-entry accounting record. Every financial event produces one or more
 * LedgerEntries, each with a debit side and a credit side.
 *
 * Standard banking categories:
 *   Assets    : RESERVES, LOANS, SECURITIES
 *   Liabilities: DEPOSITS, BORROWINGS
 *   Revenue   : INTEREST_INCOME, FEE_INCOME
 *   Expenses  : INTEREST_EXPENSE, CHARGE_OFF
 */
public class LedgerEntry {

    private final String entryId;
    private final String transactionId;
    private final LocalDate date;
    private final String debitCategory;
    private final String creditCategory;
    private final double amount;
    private final String description;

    public LedgerEntry(
            String entryId,
            String transactionId,
            LocalDate date,
            String debitCategory,
            String creditCategory,
            double amount,
            String description
    ) {
        if (entryId == null || entryId.isBlank()) throw new IllegalArgumentException("Entry ID cannot be blank.");
        if (transactionId == null || transactionId.isBlank()) throw new IllegalArgumentException("Transaction ID cannot be blank.");
        if (date == null) throw new IllegalArgumentException("Date cannot be null.");
        if (debitCategory == null || debitCategory.isBlank()) throw new IllegalArgumentException("Debit category cannot be blank.");
        if (creditCategory == null || creditCategory.isBlank()) throw new IllegalArgumentException("Credit category cannot be blank.");
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");

        this.entryId = entryId;
        this.transactionId = transactionId;
        this.date = date;
        this.debitCategory = debitCategory;
        this.creditCategory = creditCategory;
        this.amount = amount;
        this.description = (description == null) ? "" : description;
    }

    public String getEntryId() { return entryId; }
    public String getTransactionId() { return transactionId; }
    public LocalDate getDate() { return date; }
    public String getDebitCategory() { return debitCategory; }
    public String getCreditCategory() { return creditCategory; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "LedgerEntry{" +
                "entryId='" + entryId + '\'' +
                ", txId='" + transactionId + '\'' +
                ", date=" + date +
                ", DR=" + debitCategory +
                ", CR=" + creditCategory +
                ", amount=" + amount +
                '}';
    }
}
