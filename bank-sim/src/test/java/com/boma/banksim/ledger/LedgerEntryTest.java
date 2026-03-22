package com.boma.banksim.ledger;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class LedgerEntryTest {

    private static final LocalDate DATE = LocalDate.of(2024, 1, 1);

    private LedgerEntry valid() {
        return new LedgerEntry("LE1", "TX1", DATE, "RESERVES", "DEPOSITS", 1_000.0, "Deposit");
    }

    @Test
    void constructor_validArgs_setsFields() {
        LedgerEntry e = valid();
        assertEquals("LE1", e.getEntryId());
        assertEquals("TX1", e.getTransactionId());
        assertEquals(DATE, e.getDate());
        assertEquals("RESERVES", e.getDebitCategory());
        assertEquals("DEPOSITS", e.getCreditCategory());
        assertEquals(1_000.0, e.getAmount(), 0.001);
        assertEquals("Deposit", e.getDescription());
    }

    @Test
    void constructor_blankEntryId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new LedgerEntry("", "TX1", DATE, "RESERVES", "DEPOSITS", 100, ""));
    }

    @Test
    void constructor_blankTransactionId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new LedgerEntry("LE1", "", DATE, "RESERVES", "DEPOSITS", 100, ""));
    }

    @Test
    void constructor_nullDate_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new LedgerEntry("LE1", "TX1", null, "RESERVES", "DEPOSITS", 100, ""));
    }

    @Test
    void constructor_blankDebitCategory_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new LedgerEntry("LE1", "TX1", DATE, " ", "DEPOSITS", 100, ""));
    }

    @Test
    void constructor_blankCreditCategory_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new LedgerEntry("LE1", "TX1", DATE, "RESERVES", "", 100, ""));
    }

    @Test
    void constructor_zeroAmount_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new LedgerEntry("LE1", "TX1", DATE, "RESERVES", "DEPOSITS", 0, ""));
    }

    @Test
    void constructor_negativeAmount_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new LedgerEntry("LE1", "TX1", DATE, "RESERVES", "DEPOSITS", -1, ""));
    }

    @Test
    void constructor_nullDescription_defaultsToEmpty() {
        LedgerEntry e = new LedgerEntry("LE1", "TX1", DATE, "RESERVES", "DEPOSITS", 100, null);
        assertEquals("", e.getDescription());
    }
}
