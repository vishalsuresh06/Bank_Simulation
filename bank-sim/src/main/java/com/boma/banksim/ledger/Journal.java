package com.boma.banksim.ledger;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A date-filtered view of the {@link Ledger}. Useful for period reporting
 * (e.g. "show me all entries for March 2024").
 */
public class Journal {

    private final Ledger ledger;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;

    public Journal(Ledger ledger, LocalDate periodStart, LocalDate periodEnd) {
        if (ledger == null) throw new IllegalArgumentException("Ledger cannot be null.");
        if (periodStart == null) throw new IllegalArgumentException("Period start cannot be null.");
        if (periodEnd == null) throw new IllegalArgumentException("Period end cannot be null.");
        if (periodEnd.isBefore(periodStart)) throw new IllegalArgumentException("Period end cannot be before period start.");

        this.ledger = ledger;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    /** All ledger entries within this journal's period. */
    public List<LedgerEntry> getEntries() {
        return ledger.getEntriesBetween(periodStart, periodEnd);
    }

    /** Total amount debited to a given category during the period. */
    public double totalDebits(String category) {
        return getEntries().stream()
                .filter(e -> e.getDebitCategory().equals(category))
                .mapToDouble(LedgerEntry::getAmount)
                .sum();
    }

    /** Total amount credited to a given category during the period. */
    public double totalCredits(String category) {
        return getEntries().stream()
                .filter(e -> e.getCreditCategory().equals(category))
                .mapToDouble(LedgerEntry::getAmount)
                .sum();
    }

    /** Groups entries by debit category and sums amounts. */
    public Map<String, Double> debitSummary() {
        return getEntries().stream()
                .collect(Collectors.groupingBy(
                        LedgerEntry::getDebitCategory,
                        Collectors.summingDouble(LedgerEntry::getAmount)
                ));
    }

    /** Groups entries by credit category and sums amounts. */
    public Map<String, Double> creditSummary() {
        return getEntries().stream()
                .collect(Collectors.groupingBy(
                        LedgerEntry::getCreditCategory,
                        Collectors.summingDouble(LedgerEntry::getAmount)
                ));
    }

    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }

    @Override
    public String toString() {
        return "Journal{period=" + periodStart + " to " + periodEnd +
                ", entries=" + getEntries().size() + '}';
    }
}
