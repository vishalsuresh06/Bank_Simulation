package com.boma.banksim.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BankBalanceSheetTest {

    private BankBalanceSheet sheet;

    @BeforeEach
    void setUp() {
        // reserves=1_000_000, loans=500_000, deposits=600_000
        // equity = (1_000_000 + 500_000) - 600_000 = 900_000
        sheet = new BankBalanceSheet(1_000_000, 500_000, 600_000);
    }

    // ---- Construction & invariants ----

    @Test
    void constructor_negativeReserves_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new BankBalanceSheet(-1, 0, 0));
    }

    @Test
    void constructor_negativeLoans_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new BankBalanceSheet(0, -1, 0));
    }

    @Test
    void constructor_negativeDeposits_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new BankBalanceSheet(0, 0, -1));
    }

    @Test
    void getTotalAssets_equalsReservesPlusLoans() {
        assertEquals(1_500_000, sheet.getTotalAssets(), 0.01);
    }

    @Test
    void getTotalLiabilities_equalsDeposits() {
        assertEquals(600_000, sheet.getTotalLiabilities(), 0.01);
    }

    @Test
    void getEquity_assetsMinusLiabilities() {
        assertEquals(900_000, sheet.getEquity(), 0.01);
    }

    @Test
    void isBalanced_alwaysTrue() {
        // Equity is derived, so this is mathematically always true
        assertTrue(sheet.isBalanced());
    }

    @Test
    void isSolvent_positiveEquity_true() {
        assertTrue(sheet.isSolvent());
    }

    @Test
    void isSolvent_negativeEquity_false() {
        // Make liabilities exceed assets
        sheet.increaseDeposits(2_000_000); // deposits now 2_600_000 > assets 1_500_000
        assertFalse(sheet.isSolvent());
    }

    // ---- Reserves ----

    @Test
    void increaseReserves_updatesReservesAndAssets() {
        sheet.increaseReserves(100_000);
        assertEquals(1_100_000, sheet.getReserves(), 0.01);
        assertEquals(1_600_000, sheet.getTotalAssets(), 0.01);
    }

    @Test
    void increaseReserves_equityAlsoIncreases() {
        double equityBefore = sheet.getEquity();
        sheet.increaseReserves(100_000);
        assertEquals(equityBefore + 100_000, sheet.getEquity(), 0.01);
    }

    @Test
    void increaseReserves_zero_throws() {
        assertThrows(IllegalArgumentException.class, () -> sheet.increaseReserves(0));
    }

    @Test
    void increaseReserves_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> sheet.increaseReserves(-100));
    }

    @Test
    void decreaseReserves_updatesReservesAndAssets() {
        sheet.decreaseReserves(200_000);
        assertEquals(800_000, sheet.getReserves(), 0.01);
    }

    @Test
    void decreaseReserves_belowZero_throws() {
        assertThrows(IllegalArgumentException.class, () -> sheet.decreaseReserves(2_000_000));
    }

    @Test
    void decreaseReserves_exact_reducesToZero() {
        sheet.decreaseReserves(1_000_000);
        assertEquals(0.0, sheet.getReserves(), 0.01);
    }

    // ---- Loans ----

    @Test
    void increaseLoans_updatesLoansAndAssets() {
        sheet.increaseLoans(100_000);
        assertEquals(600_000, sheet.getTotalLoans(), 0.01);
        assertEquals(1_600_000, sheet.getTotalAssets(), 0.01);
    }

    @Test
    void decreaseLoans_belowZero_throws() {
        assertThrows(IllegalArgumentException.class, () -> sheet.decreaseLoans(1_000_000));
    }

    // ---- Deposits ----

    @Test
    void increaseDeposits_updatesDepositsAndLiabilities() {
        sheet.increaseDeposits(50_000);
        assertEquals(650_000, sheet.getTotalDeposits(), 0.01);
    }

    @Test
    void increaseDeposits_reducesEquity() {
        double equityBefore = sheet.getEquity();
        sheet.increaseDeposits(50_000);
        assertEquals(equityBefore - 50_000, sheet.getEquity(), 0.01);
    }

    @Test
    void decreaseDeposits_belowZero_throws() {
        assertThrows(IllegalArgumentException.class, () -> sheet.decreaseDeposits(700_000));
    }

    // ---- Income tracking ----

    @Test
    void recordInterestIncome_accumulatesCorrectly() {
        sheet.recordInterestIncome(100);
        sheet.recordInterestIncome(50);
        assertEquals(150, sheet.getGrossInterestIncome(), 0.001);
    }

    @Test
    void recordDepositInterestExpense_accumulatesCorrectly() {
        sheet.recordDepositInterestExpense(30);
        sheet.recordDepositInterestExpense(20);
        assertEquals(50, sheet.getDepositInterestExpense(), 0.001);
    }

    @Test
    void recordChargeOff_accumulatesCorrectly() {
        sheet.recordChargeOff(10_000);
        sheet.recordChargeOff(5_000);
        assertEquals(15_000, sheet.getChargeOffLosses(), 0.001);
    }

    @Test
    void getNetInterestIncome_isGrossMinusExpense() {
        sheet.recordInterestIncome(500);
        sheet.recordDepositInterestExpense(200);
        assertEquals(300, sheet.getNetInterestIncome(), 0.001);
    }

    @Test
    void getNetIncome_isNIIMinusChargeOffs() {
        sheet.recordInterestIncome(1_000);
        sheet.recordDepositInterestExpense(200);
        sheet.recordChargeOff(300);
        assertEquals(500, sheet.getNetIncome(), 0.001);
    }

    @Test
    void resetPeriodCounters_zerosAllIncomeFields() {
        sheet.recordInterestIncome(1_000);
        sheet.recordDepositInterestExpense(300);
        sheet.recordChargeOff(500);
        sheet.resetPeriodCounters();
        assertEquals(0, sheet.getGrossInterestIncome(), 0.001);
        assertEquals(0, sheet.getDepositInterestExpense(), 0.001);
        assertEquals(0, sheet.getChargeOffLosses(), 0.001);
    }

    @Test
    void balanceSheet_loanOrigination_equityUnchanged() {
        // Loan origination: loans↑ reserves↓ → assets neutral, equity unchanged
        double equityBefore = sheet.getEquity();
        sheet.increaseLoans(200_000);
        sheet.decreaseReserves(200_000);
        assertEquals(equityBefore, sheet.getEquity(), 0.01);
    }

    @Test
    void balanceSheet_deposit_equityUnchanged() {
        // Customer deposit: reserves↑ deposits↑ → assets +100, liabilities +100, equity unchanged
        double equityBefore = sheet.getEquity();
        sheet.increaseReserves(100_000);
        sheet.increaseDeposits(100_000);
        assertEquals(equityBefore, sheet.getEquity(), 0.01);
    }

    @Test
    void balanceSheet_chargeOff_equityDecreases() {
        // Charge-off: loans↓ → assets decrease, liabilities unchanged, equity decreases
        double equityBefore = sheet.getEquity();
        sheet.decreaseLoans(50_000);
        assertEquals(equityBefore - 50_000, sheet.getEquity(), 0.01);
    }
}
