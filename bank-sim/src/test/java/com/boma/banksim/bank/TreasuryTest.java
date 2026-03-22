package com.boma.banksim.bank;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TreasuryTest {

    @Test
    void constructor_negativeSecurities_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Treasury(-1, 0));
    }

    @Test
    void constructor_negativeBorrowings_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Treasury(0, -1));
    }

    @Test
    void defaultConstructor_allZero() {
        Treasury t = new Treasury();
        assertEquals(0.0, t.getSecurities(), 0.001);
        assertEquals(0.0, t.getBorrowings(), 0.001);
    }

    @Test
    void purchaseSecurities_increasesSecurities() {
        Treasury t = new Treasury();
        t.purchaseSecurities(500_000);
        assertEquals(500_000, t.getSecurities(), 0.001);
    }

    @Test
    void purchaseSecurities_zero_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Treasury().purchaseSecurities(0));
    }

    @Test
    void sellSecurities_decreasesSecurities() {
        Treasury t = new Treasury(1_000_000, 0);
        t.sellSecurities(300_000);
        assertEquals(700_000, t.getSecurities(), 0.001);
    }

    @Test
    void sellSecurities_exact_reducesToZero() {
        Treasury t = new Treasury(500_000, 0);
        t.sellSecurities(500_000);
        assertEquals(0.0, t.getSecurities(), 0.001);
    }

    @Test
    void sellSecurities_moreThanHeld_throws() {
        Treasury t = new Treasury(100_000, 0);
        assertThrows(IllegalArgumentException.class, () -> t.sellSecurities(200_000));
    }

    @Test
    void borrow_increasesBorrowings() {
        Treasury t = new Treasury();
        t.borrow(1_000_000);
        assertEquals(1_000_000, t.getBorrowings(), 0.001);
    }

    @Test
    void borrow_zero_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Treasury().borrow(0));
    }

    @Test
    void repayBorrowing_decreasesBorrowings() {
        Treasury t = new Treasury(0, 500_000);
        t.repayBorrowing(200_000);
        assertEquals(300_000, t.getBorrowings(), 0.001);
    }

    @Test
    void repayBorrowing_moreThanOwed_throws() {
        Treasury t = new Treasury(0, 100_000);
        assertThrows(IllegalArgumentException.class, () -> t.repayBorrowing(200_000));
    }

    @Test
    void netPosition_securitiesMinusBorrowings() {
        Treasury t = new Treasury(500_000, 200_000);
        assertEquals(300_000, t.netPosition(), 0.001);
    }

    @Test
    void netPosition_negativWhenBorrowingsExceedSecurities() {
        Treasury t = new Treasury(100_000, 400_000);
        assertEquals(-300_000, t.netPosition(), 0.001);
    }
}
