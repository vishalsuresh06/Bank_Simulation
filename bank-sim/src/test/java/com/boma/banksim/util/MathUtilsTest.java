package com.boma.banksim.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MathUtilsTest {

    @Test
    void monthlyPayment_standardMortgage_correctValue() {
        // Classic 30-year, 6% annual mortgage benchmark: ~$599.55
        double payment = MathUtils.monthlyPayment(100_000, 0.06, 360);
        assertEquals(599.55, payment, 0.01);
    }

    @Test
    void monthlyPayment_zeroRate_equalInstallments() {
        double payment = MathUtils.monthlyPayment(12_000, 0.0, 12);
        assertEquals(1_000.0, payment, 0.001);
    }

    @Test
    void monthlyPayment_singleMonth_principalPlusInterest() {
        double payment = MathUtils.monthlyPayment(1_200, 0.12, 1);
        // One month of 12% annual = 1% monthly → 1200 * 1.01 = 1212
        assertEquals(1_212.0, payment, 0.01);
    }

    @Test
    void monthlyPayment_resultIsPositive() {
        assertTrue(MathUtils.monthlyPayment(50_000, 0.07, 60) > 0);
    }

    @Test
    void round2_roundsDown() {
        assertEquals(1.23, MathUtils.round2(1.234), 0.0001);
    }

    @Test
    void round2_roundsUp() {
        assertEquals(1.24, MathUtils.round2(1.235), 0.0001);
    }

    @Test
    void round2_exactValue_unchanged() {
        assertEquals(5.50, MathUtils.round2(5.50), 0.0001);
    }

    @Test
    void round2_zero() {
        assertEquals(0.0, MathUtils.round2(0.0), 0.0001);
    }

    @Test
    void clamp_belowMin_returnsMin() {
        assertEquals(0.0, MathUtils.clamp(-1.0, 0.0, 1.0), 0.0001);
    }

    @Test
    void clamp_aboveMax_returnsMax() {
        assertEquals(1.0, MathUtils.clamp(2.0, 0.0, 1.0), 0.0001);
    }

    @Test
    void clamp_withinRange_returnsValue() {
        assertEquals(0.5, MathUtils.clamp(0.5, 0.0, 1.0), 0.0001);
    }

    @Test
    void clamp_equalToMin_returnsMin() {
        assertEquals(0.0, MathUtils.clamp(0.0, 0.0, 1.0), 0.0001);
    }

    @Test
    void clamp_equalToMax_returnsMax() {
        assertEquals(1.0, MathUtils.clamp(1.0, 0.0, 1.0), 0.0001);
    }

    @Test
    void debtToIncome_normal() {
        assertEquals(0.25, MathUtils.debtToIncome(500, 2_000), 0.0001);
    }

    @Test
    void debtToIncome_zeroIncome_returnsMaxValue() {
        assertEquals(Double.MAX_VALUE, MathUtils.debtToIncome(500, 0));
    }

    @Test
    void debtToIncome_zeroDebt_returnsZero() {
        assertEquals(0.0, MathUtils.debtToIncome(0, 2_000), 0.0001);
    }
}
