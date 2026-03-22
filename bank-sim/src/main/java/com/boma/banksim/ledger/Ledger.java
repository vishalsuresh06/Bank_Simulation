package com.boma.banksim.ledger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The bank's general ledger — an append-only log of double-entry accounting records.
 * Every financial transaction produces one or more {@link LedgerEntry} objects here.
 */
public class Ledger {

    private final List<LedgerEntry> entries;

    public Ledger() {
        this.entries = new ArrayList<>();
    }

    public void addEntry(LedgerEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("LedgerEntry cannot be null.");
        }
        entries.add(entry);
    }

    public List<LedgerEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public int getEntryCount() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /** Returns all entries on or after {@code from} and before or on {@code to}. */
    public List<LedgerEntry> getEntriesBetween(LocalDate from, LocalDate to) {
        return entries.stream()
                .filter(e -> !e.getDate().isBefore(from) && !e.getDate().isAfter(to))
                .collect(Collectors.toList());
    }

    /** Returns all entries for a specific transaction. */
    public List<LedgerEntry> getEntriesForTransaction(String transactionId) {
        return entries.stream()
                .filter(e -> e.getTransactionId().equals(transactionId))
                .collect(Collectors.toList());
    }

    /**
     * Sums all amounts where the given category is on the debit side minus
     * amounts where it is on the credit side. Useful for account balances.
     */
    public double netBalance(String category) {
        double debits = entries.stream()
                .filter(e -> e.getDebitCategory().equals(category))
                .mapToDouble(LedgerEntry::getAmount)
                .sum();
        double credits = entries.stream()
                .filter(e -> e.getCreditCategory().equals(category))
                .mapToDouble(LedgerEntry::getAmount)
                .sum();
        return debits - credits;
    }

    @Override
    public String toString() {
        return "Ledger{entryCount=" + entries.size() + '}';
    }
}
