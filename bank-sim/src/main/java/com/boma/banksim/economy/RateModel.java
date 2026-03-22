package com.boma.banksim.economy;

import com.boma.banksim.loan.LoanType;

/**
 * Computes interest rates for loans and savings accounts based on the
 * prevailing economic environment.
 *
 * Loan rate    = policyRate + creditSpread (higher for riskier loan types)
 * Savings rate = policyRate − depositMargin (bank captures a spread)
 */
public class RateModel {

    private RateModel() {}

    /**
     * Annual interest rate charged on a new loan.
     *
     * Spread by type:
     *   CONSUMER  +3%
     *   MORTGAGE  +1.5%
     *   BUSINESS  +2.5%
     *
     * During a recession/crisis the bank adds a 1% risk premium.
     */
    public static double loanRate(EconomicEnvironment economy, LoanType type) {
        double base = economy.getPolicyRate();
        double spread = switch (type) {
            case CONSUMER -> 0.03;
            case MORTGAGE -> 0.015;
            case BUSINESS -> 0.025;
        };
        double riskPremium = economy.isStressed() ? 0.01 : 0.0;
        return base + spread + riskPremium;
    }

    /**
     * Annual interest rate paid on savings accounts.
     * = policyRate − 1.5%  (bank's deposit margin), floored at 0.
     */
    public static double savingsRate(EconomicEnvironment economy) {
        return Math.max(0.0, economy.getPolicyRate() - 0.015);
    }

    /**
     * Adjusts a borrower's rate based on credit score.
     * Scores below 650 attract an additional premium.
     */
    public static double adjustForCreditScore(double baseRate, double creditScore) {
        if (creditScore >= 750) return baseRate;
        if (creditScore >= 700) return baseRate + 0.005;
        if (creditScore >= 650) return baseRate + 0.010;
        if (creditScore >= 600) return baseRate + 0.020;
        return baseRate + 0.035;
    }
}
