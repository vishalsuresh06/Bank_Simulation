package com.boma.banksim.risk;

import com.boma.banksim.customer.CustomerProfile;
import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.util.MathUtils;

/**
 * Estimates the fraction of deposits a customer might withdraw unexpectedly
 * in a stress scenario (bank run / panic withdrawal behaviour).
 *
 * The stressed withdrawal rate increases with:
 *   - Low depositor confidence in the economy
 *   - High individual withdrawal sensitivity
 *   - Recession / crisis economic state
 */
public class LiquidityStressModel {

    /**
     * Base monthly stressed withdrawal rate for a given customer in the
     * current economic environment.
     *
     * Range: 0% (normal, insensitive customer) – ~30% (crisis, max sensitivity)
     */
    public double stressedWithdrawalRate(EconomicEnvironment economy, CustomerProfile profile) {
        double confidence = economy.getDepositorConfidence();
        double sensitivity = profile.getWithdrawalSensitivity();

        // Panic factor: 0 when full confidence, 1 when zero confidence
        double panicFactor = 1.0 - confidence;

        // Economic multiplier
        double economicMultiplier = switch (economy.getState()) {
            case EXPANSION -> 0.5;
            case NORMAL -> 1.0;
            case RECESSION -> 1.5;
            case CRISIS -> 2.5;
        };

        double rate = panicFactor * sensitivity * economicMultiplier * 0.30;
        return MathUtils.clamp(rate, 0.0, 0.30);
    }

    /**
     * Returns the stressed withdrawal amount given the account balance.
     */
    public double stressedWithdrawalAmount(double balance, EconomicEnvironment economy, CustomerProfile profile) {
        return balance * stressedWithdrawalRate(economy, profile);
    }
}
