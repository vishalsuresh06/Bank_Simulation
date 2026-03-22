package com.boma.banksim.ledger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LedgerTest {

    private Ledger ledger;
    private static final LocalDate JAN = LocalDate.of(2024, 1, 15);
    private static final LocalDate FEB = LocalDate.of(2024, 2, 15);
    private static final LocalDate MAR = LocalDate.of(2024, 3, 15);

    @BeforeEach
    void setUp() {
        ledger = new Ledger();
    }

    private LedgerEntry entry(String txId, LocalDate date, String dr, String cr, double amount) {
        return new LedgerEntry("LE-" + txId + date, txId, date, dr, cr, amount, "");
    }

    @Test
    void newLedger_isEmpty() {
        assertTrue(ledger.isEmpty());
        assertEquals(0, ledger.getEntryCount());
    }

    @Test
    void addEntry_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> ledger.addEntry(null));
    }

    @Test
    void addEntry_incrementsCount() {
        ledger.addEntry(entry("TX1", JAN, "RESERVES", "DEPOSITS", 1_000));
        assertEquals(1, ledger.getEntryCount());
        assertFalse(ledger.isEmpty());
    }

    @Test
    void getEntries_returnsUnmodifiableList() {
        List<LedgerEntry> entries = ledger.getEntries();
        assertThrows(UnsupportedOperationException.class,
                () -> entries.add(entry("TX1", JAN, "RESERVES", "DEPOSITS", 100)));
    }

    @Test
    void getEntriesBetween_returnsOnlyInRange() {
        ledger.addEntry(entry("TX1", JAN, "RESERVES", "DEPOSITS", 100)); // included
        ledger.addEntry(entry("TX2", FEB, "RESERVES", "DEPOSITS", 200)); // included
        ledger.addEntry(entry("TX3", MAR, "RESERVES", "DEPOSITS", 300)); // excluded

        List<LedgerEntry> filtered = ledger.getEntriesBetween(JAN, FEB);
        assertEquals(2, filtered.size());
    }

    @Test
    void getEntriesBetween_inclusiveBothEnds() {
        ledger.addEntry(entry("TX1", JAN, "RESERVES", "DEPOSITS", 100));
        ledger.addEntry(entry("TX2", FEB, "RESERVES", "DEPOSITS", 200));

        List<LedgerEntry> filtered = ledger.getEntriesBetween(JAN, JAN);
        assertEquals(1, filtered.size());
    }

    @Test
    void getEntriesForTransaction_returnsMatchingTransactionEntries() {
        ledger.addEntry(entry("TX1", JAN, "RESERVES", "DEPOSITS", 100));
        ledger.addEntry(entry("TX1", FEB, "RESERVES", "INTEREST_INCOME", 10));
        ledger.addEntry(entry("TX2", JAN, "RESERVES", "DEPOSITS", 500));

        List<LedgerEntry> txEntries = ledger.getEntriesForTransaction("TX1");
        assertEquals(2, txEntries.size());
    }

    @Test
    void netBalance_debitMinusCredit() {
        ledger.addEntry(entry("TX1", JAN, "RESERVES", "DEPOSITS", 100));  // debit RESERVES
        ledger.addEntry(entry("TX2", JAN, "RESERVES", "DEPOSITS", 200));  // debit RESERVES
        ledger.addEntry(entry("TX3", JAN, "DEPOSITS", "RESERVES", 50));   // credit RESERVES

        assertEquals(250, ledger.netBalance("RESERVES"), 0.01); // 100+200-50
    }

    @Test
    void netBalance_unknownCategory_returnsZero() {
        ledger.addEntry(entry("TX1", JAN, "RESERVES", "DEPOSITS", 100));
        assertEquals(0.0, ledger.netBalance("UNKNOWN_CATEGORY"), 0.001);
    }

    @Test
    void multipleEntries_countCorrect() {
        for (int i = 0; i < 5; i++) {
            ledger.addEntry(entry("TX" + i, JAN, "RESERVES", "DEPOSITS", 100 * (i + 1)));
        }
        assertEquals(5, ledger.getEntryCount());
    }
}
