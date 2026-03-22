package com.boma.banksim.risk;

import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.loan.Loan;

/**
 * Estimates the expected credit loss for a loan portfolio.
 *
 * Expected Loss (EL) = PD × LGD × EAD
 *   PD  – Probability of Default (from DefaultModel)
 *   LGD – Loss Given Default (fraction of outstanding balance the bank won't recover)
 *   EAD – Exposure at Default (outstanding balance)
 */
public class CreditRiskModel {

    private final DefaultModel defaultModel;

    public CreditRiskModel() {
        this.defaultModel = new DefaultModel();
    }

    /**
     * LGD by loan type:
     *   MORTGAGE → 25% (collateral recovery via foreclosure)
     *   CONSUMER → 70% (unsecured — little recovery)
     *   BUSINESS → 50% (mixed collateral)
     */
    public double lossGivenDefault(Loan loan) {
        return switch (loan.getType()) {
            case MORTGAGE -> 0.25;
            case CONSUMER -> 0.70;
            case BUSINESS -> 0.50;
        };
    }

    /** Expected Loss for a single loan. */
    public double expectedLoss(Loan loan, EconomicEnvironment economy) {
        double pd = defaultModel.defaultProbability(loan, economy);
        double lgd = lossGivenDefault(loan);
        double ead = loan.getOutstandingBalance();
        return pd * lgd * ead;
    }

    /** Total expected loss across all loans in the collection. */
    public double totalExpectedLoss(Iterable<Loan> loans, EconomicEnvironment economy) {
        double total = 0;
        for (Loan l : loans) {
            total += expectedLoss(l, economy);
        }
        return total;
    }
}
