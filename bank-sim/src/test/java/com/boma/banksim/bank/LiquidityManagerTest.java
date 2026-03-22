package com.boma.banksim.bank;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LiquidityManagerTest {

    private BankBalanceSheet sheet(double reserves, double loans, double deposits) {
        return new BankBalanceSheet(reserves, loans, deposits);
    }

    @Test
    void constructor_negativeRatio_throws() {
        assertThrows(IllegalArgumentException.class, () -> new LiquidityManager(-0.01));
    }

    @Test
    void constructor_ratioAboveOne_throws() {
        assertThrows(IllegalArgumentException.class, () -> new LiquidityManager(1.01));
    }

    @Test
    void defaultConstructor_tenPercentRatio() {
        assertEquals(0.10, new LiquidityManager().getMinimumReserveRatio(), 0.0001);
    }

    @Test
    void requiredReserves_correctCalculation() {
        LiquidityManager lm = new LiquidityManager(0.10);
        BankBalanceSheet bs = sheet(500_000, 200_000, 1_000_000);
        assertEquals(100_000, lm.requiredReserves(bs), 0.01);
    }

    @Test
    void excessReserves_adequate_positive() {
        LiquidityManager lm = new LiquidityManager(0.10);
        // reserves=200_000, required=100_000 → excess=100_000
        BankBalanceSheet bs = sheet(200_000, 0, 1_000_000);
        assertEquals(100_000, lm.excessReserves(bs), 0.01);
    }

    @Test
    void excessReserves_deficit_negative() {
        LiquidityManager lm = new LiquidityManager(0.10);
        // reserves=50_000, required=100_000 → excess=-50_000
        BankBalanceSheet bs = sheet(50_000, 0, 1_000_000);
        assertEquals(-50_000, lm.excessReserves(bs), 0.01);
    }

    @Test
    void reserveRatio_correctCalculation() {
        LiquidityManager lm = new LiquidityManager(0.10);
        BankBalanceSheet bs = sheet(200_000, 0, 1_000_000);
        assertEquals(0.20, lm.reserveRatio(bs), 0.0001);
    }

    @Test
    void reserveRatio_zeroDeposits_returnsMaxDouble() {
        LiquidityManager lm = new LiquidityManager(0.10);
        BankBalanceSheet bs = sheet(100_000, 0, 0);
        assertEquals(Double.MAX_VALUE, lm.reserveRatio(bs));
    }

    @Test
    void isAdequate_exactlyMeetsMinimum_true() {
        LiquidityManager lm = new LiquidityManager(0.10);
        BankBalanceSheet bs = sheet(100_000, 0, 1_000_000); // reserves == required
        assertTrue(lm.isAdequate(bs));
    }

    @Test
    void isAdequate_exceedsMinimum_true() {
        LiquidityManager lm = new LiquidityManager(0.10);
        BankBalanceSheet bs = sheet(200_000, 0, 1_000_000);
        assertTrue(lm.isAdequate(bs));
    }

    @Test
    void isAdequate_belowMinimum_false() {
        LiquidityManager lm = new LiquidityManager(0.10);
        BankBalanceSheet bs = sheet(50_000, 0, 1_000_000);
        assertFalse(lm.isAdequate(bs));
    }

    @Test
    void isUnderReserved_deficient_true() {
        LiquidityManager lm = new LiquidityManager(0.10);
        BankBalanceSheet bs = sheet(50_000, 0, 1_000_000);
        assertTrue(lm.isUnderReserved(bs));
    }
}
