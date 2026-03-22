package com.boma.banksim.ledger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class JournalTest {

    private Ledger ledger;
    private static final LocalDate JAN1 = LocalDate.of(2024, 1, 1);
    private static final LocalDate JAN31 = LocalDate.of(2024, 1, 31);
    private static final LocalDate FEB15 = LocalDate.of(2024, 2, 15);

    @BeforeEach
    void setUp() {
        ledger = new Ledger();
        // January entries
        ledger.addEntry(new LedgerEntry("E1", "TX1", JAN1,   "RESERVES", "DEPOSITS", 1_000, ""));
        ledger.addEntry(new LedgerEntry("E2", "TX2", JAN31,  "RESERVES", "DEPOSITS", 2_000, ""));
        ledger.addEntry(new LedgerEntry("E3", "TX3", JAN1,   "RESERVES", "INTEREST_INCOME", 100, ""));
        // February entry
        ledger.addEntry(new LedgerEntry("E4", "TX4", FEB15,  "RESERVES", "DEPOSITS", 500, ""));
    }

    @Test
    void constructor_endBeforeStart_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Journal(ledger, JAN31, JAN1));
    }

    @Test
    void constructor_nullLedger_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Journal(null, JAN1, JAN31));
    }

    @Test
    void getEntries_filtersByPeriod() {
        Journal jan = new Journal(ledger, JAN1, JAN31);
        assertEquals(3, jan.getEntries().size()); // JAN1 x2 and JAN31
    }

    @Test
    void getEntries_excludesOutsidePeriod() {
        Journal jan = new Journal(ledger, JAN1, JAN31);
        assertTrue(jan.getEntries().stream()
                .noneMatch(e -> e.getDate().isAfter(JAN31)));
    }

    @Test
    void totalDebits_sumsByCategory() {
        Journal jan = new Journal(ledger, JAN1, JAN31);
        // 3 entries all debit RESERVES: 1000 + 2000 + 100 = 3100
        assertEquals(3_100, jan.totalDebits("RESERVES"), 0.01);
    }

    @Test
    void totalCredits_sumsByCategory() {
        Journal jan = new Journal(ledger, JAN1, JAN31);
        // 2 credit DEPOSITS: 1000 + 2000 = 3000
        assertEquals(3_000, jan.totalCredits("DEPOSITS"), 0.01);
    }

    @Test
    void totalCredits_unknownCategory_returnsZero() {
        Journal jan = new Journal(ledger, JAN1, JAN31);
        assertEquals(0.0, jan.totalCredits("LOANS"), 0.001);
    }

    @Test
    void debitSummary_groupsCorrectly() {
        Journal jan = new Journal(ledger, JAN1, JAN31);
        Map<String, Double> summary = jan.debitSummary();
        assertTrue(summary.containsKey("RESERVES"));
        assertEquals(3_100, summary.get("RESERVES"), 0.01);
    }

    @Test
    void creditSummary_groupsCorrectly() {
        Journal jan = new Journal(ledger, JAN1, JAN31);
        Map<String, Double> summary = jan.creditSummary();
        assertTrue(summary.containsKey("DEPOSITS"));
        assertTrue(summary.containsKey("INTEREST_INCOME"));
        assertEquals(3_000, summary.get("DEPOSITS"), 0.01);
        assertEquals(100, summary.get("INTEREST_INCOME"), 0.01);
    }

    @Test
    void emptyPeriod_allMethodsReturnZeroOrEmpty() {
        LocalDate far = LocalDate.of(2025, 1, 1);
        Journal empty = new Journal(ledger, far, far.plusDays(1));
        assertEquals(0, empty.getEntries().size());
        assertEquals(0.0, empty.totalDebits("RESERVES"), 0.001);
        assertTrue(empty.debitSummary().isEmpty());
        assertTrue(empty.creditSummary().isEmpty());
    }

    @Test
    void accessors_returnCorrectDates() {
        Journal j = new Journal(ledger, JAN1, JAN31);
        assertEquals(JAN1, j.getPeriodStart());
        assertEquals(JAN31, j.getPeriodEnd());
    }
}
