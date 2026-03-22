package com.boma.banksim.transaction;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2024, 1, 15, 12, 0);

    private Transaction deposit() {
        return new Transaction("TX1", TransactionType.DEPOSIT, 1_000,
                NOW, null, "ACC1", "deposit");
    }

    @Test
    void constructor_validDeposit_setsFields() {
        Transaction tx = deposit();
        assertEquals("TX1", tx.getTransactionId());
        assertEquals(TransactionType.DEPOSIT, tx.getType());
        assertEquals(1_000, tx.getAmount(), 0.001);
        assertEquals(NOW, tx.getTimestamp());
        assertNull(tx.getSourceAccountId());
        assertEquals("ACC1", tx.getDestinationAccountId());
        assertEquals("deposit", tx.getDescription());
    }

    @Test
    void constructor_bothAccountsNull_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction("TX1", TransactionType.DEPOSIT, 1_000, NOW, null, null, ""));
    }

    @Test
    void constructor_oneAccountSufficient_noException() {
        assertDoesNotThrow(
                () -> new Transaction("TX1", TransactionType.DEPOSIT, 500, NOW, null, "ACC1", ""));
    }

    @Test
    void constructor_zeroAmount_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction("TX1", TransactionType.DEPOSIT, 0, NOW, null, "ACC1", ""));
    }

    @Test
    void constructor_negativeAmount_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction("TX1", TransactionType.DEPOSIT, -100, NOW, null, "ACC1", ""));
    }

    @Test
    void constructor_nullType_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction("TX1", null, 100, NOW, null, "ACC1", ""));
    }

    @Test
    void constructor_nullTimestamp_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction("TX1", TransactionType.DEPOSIT, 100, null, null, "ACC1", ""));
    }

    @Test
    void constructor_blankId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction("", TransactionType.DEPOSIT, 100, NOW, null, "ACC1", ""));
    }

    @Test
    void constructor_nullDescription_defaultsToEmpty() {
        Transaction tx = new Transaction("TX1", TransactionType.DEPOSIT, 100, NOW, null, "ACC1", null);
        assertEquals("", tx.getDescription());
    }

    @Test
    void allTransactionTypes_canBeConstructed() {
        for (TransactionType type : TransactionType.values()) {
            assertDoesNotThrow(() ->
                new Transaction("TX-" + type, type, 100, NOW, "ACC1", "ACC2", "test"));
        }
    }
}
