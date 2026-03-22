package com.boma.banksim.risk;

import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.loan.Loan;
import com.boma.banksim.loan.LoanStatus;

/**
 * Estimates the monthly probability that a loan will default (stop paying)
 * based on its current delinquency status and the macroeconomic environment.
 *
 * Returns a probability in [0.0, 1.0].
 */
public class DefaultModel {

    /**
     * Monthly default probability for a given loan given the economic environment.
     *
     * Base rates by status:
     *   CURRENT     → 0.2%
     *   LATE        → 5%
     *   DELINQUENT  → 20%
     *   DEFAULTED   → 60%
     *
     * Stressed economy multiplier: 1.5× during RECESSION, 2.5× during CRISIS.
     */
    public double defaultProbability(Loan loan, EconomicEnvironment economy) {
        if (loan.getStatus() == LoanStatus.CHARGED_OFF || loan.getStatus() == LoanStatus.CLOSED) {
            return 0.0;
        }

        double baseProb = switch (loan.getStatus()) {
            case CURRENT -> 0.002;
            case LATE -> 0.05;
            case DELINQUENT -> 0.20;
            case DEFAULTED -> 0.60;
            default -> 0.0;
        };

        double multiplier = switch (economy.getState()) {
            case NORMAL -> 1.0;
            case EXPANSION -> 0.7;
            case RECESSION -> 1.5;
            case CRISIS -> 2.5;
        };

        return Math.min(1.0, baseProb * multiplier);
    }

    /**
     * Returns true if the loan should default this month (stochastic decision).
     * Use a separate {@link com.boma.banksim.util.RandomProvider} for reproducibility.
     */
    public boolean shouldDefault(Loan loan, EconomicEnvironment economy, double randomValue) {
        return randomValue < defaultProbability(loan, economy);
    }
}
