package com.boma.banksim.risk;

import com.boma.banksim.customer.CustomerProfile;
import com.boma.banksim.economy.EconomicEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LiquidityStressModelTest {

    private LiquidityStressModel model;

    @BeforeEach
    void setUp() {
        model = new LiquidityStressModel();
    }

    private CustomerProfile profile(double withdrawalSensitivity) {
        return new CustomerProfile(60_000, 0.6, 700, withdrawalSensitivity, 0.3);
    }

    @Test
    void stressedWithdrawalRate_fullConfidence_returnsZero() {
        // confidence=1.0 → panicFactor=0 → rate=0
        EconomicEnvironment e = new com.boma.banksim.economy.EconomicEnvironment(
                0.05, 0.02, 0.04, com.boma.banksim.economy.EconomicState.NORMAL, 1.0);
        assertEquals(0.0, model.stressedWithdrawalRate(e, profile(1.0)), 0.0001);
    }

    @Test
    void stressedWithdrawalRate_zeroSensitivity_returnsZero() {
        // sensitivity=0 → panic contribution=0
        assertEquals(0.0, model.stressedWithdrawalRate(EconomicEnvironment.recession(), profile(0.0)), 0.0001);
    }

    @Test
    void stressedWithdrawalRate_crisisMaxSensitivity_cappedAt30pct() {
        // max possible: panicFactor=1-0 (full panic), sensitivity=1.0, crisis=2.5, base=0.30
        // = 1.0 * 1.0 * 2.5 * 0.30 = 0.75 → clamped to 0.30
        EconomicEnvironment e = new com.boma.banksim.economy.EconomicEnvironment(
                0.10, 0.06, 0.15, com.boma.banksim.economy.EconomicState.CRISIS, 0.0);
        double rate = model.stressedWithdrawalRate(e, profile(1.0));
        assertEquals(0.30, rate, 0.0001);
    }

    @Test
    void stressedWithdrawalRate_neverExceedsThirtyPct() {
        // Run many combinations and check cap
        EconomicEnvironment crisis = EconomicEnvironment.crisis();
        CustomerProfile maxSensitivity = profile(1.0);
        double rate = model.stressedWithdrawalRate(crisis, maxSensitivity);
        assertTrue(rate <= 0.30, "Rate exceeded cap: " + rate);
    }

    @Test
    void stressedWithdrawalRate_expansionLowerThanRecession() {
        CustomerProfile p = profile(0.7);
        double expansion = model.stressedWithdrawalRate(EconomicEnvironment.expansion(), p);
        double recession = model.stressedWithdrawalRate(EconomicEnvironment.recession(), p);
        assertTrue(expansion < recession);
    }

    @Test
    void stressedWithdrawalAmount_scalesWithBalance() {
        // Use a deterministic scenario: confidence=0.5, RECESSION, sensitivity=1.0
        EconomicEnvironment e = EconomicEnvironment.recession(); // confidence=0.75
        CustomerProfile p = profile(1.0);
        double balance = 10_000;
        double amount = model.stressedWithdrawalAmount(balance, e, p);
        double rate = model.stressedWithdrawalRate(e, p);
        assertEquals(balance * rate, amount, 0.001);
    }

    @Test
    void stressedWithdrawalRate_normalEconomy_lowRate() {
        // Normal economy with some sensitivity should have low but non-zero rate
        EconomicEnvironment e = EconomicEnvironment.normal(); // confidence=0.95 → panic=0.05
        double rate = model.stressedWithdrawalRate(e, profile(0.5));
        assertTrue(rate >= 0.0 && rate < 0.05);
    }
}
