package com.boma.banksim.risk;

import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.economy.RateModel;
import com.boma.banksim.loan.LoanApplication;
import com.boma.banksim.util.MathUtils;

/**
 * Decides whether to approve a loan application.
 *
 * Criteria:
 *   1. Credit score ≥ minimum (stricter in recession)
 *   2. Debt-to-income ratio ≤ maximum (stricter in recession)
 *   3. Requested amount ≤ income-based cap
 */
public class UnderwritingEngine {

    // Standard thresholds
    private static final double MIN_CREDIT_SCORE_NORMAL = 620;
    private static final double MIN_CREDIT_SCORE_STRESSED = 680;
    private static final double MAX_DTI_NORMAL = 0.43;   // 43% DTI cap (QM standard)
    private static final double MAX_DTI_STRESSED = 0.36; // tighter during recession

    /** Evaluates and updates the application status in-place. */
    public void evaluate(LoanApplication application, EconomicEnvironment economy) {
        boolean stressed = economy.isStressed();
        double minScore = stressed ? MIN_CREDIT_SCORE_STRESSED : MIN_CREDIT_SCORE_NORMAL;
        double maxDti = stressed ? MAX_DTI_STRESSED : MAX_DTI_NORMAL;

        // 1. Credit score check
        if (application.getCreditScore() < minScore) {
            application.reject("Credit score " + application.getCreditScore() +
                    " below minimum of " + minScore);
            return;
        }

        // 2. Debt-to-income check (include projected new loan payment)
        double projectedRate = RateModel.loanRate(economy, application.getType());
        projectedRate = RateModel.adjustForCreditScore(projectedRate, application.getCreditScore());
        double projectedPayment = MathUtils.monthlyPayment(
                application.getRequestedAmount(), projectedRate, application.getRequestedTermMonths());

        double totalMonthlyDebt = application.getExistingMonthlyDebtPayments() + projectedPayment;
        double dti = MathUtils.debtToIncome(totalMonthlyDebt, application.getMonthlyIncome());

        if (dti > maxDti) {
            application.reject(String.format("DTI %.1f%% exceeds maximum %.1f%%", dti * 100, maxDti * 100));
            return;
        }

        // 3. Amount cap: no more than 5× annual income
        double maxAmount = application.getMonthlyIncome() * 12 * 5;
        if (application.getRequestedAmount() > maxAmount) {
            application.reject("Requested amount exceeds 5× annual income cap.");
            return;
        }

        application.approve();
    }

    /** Returns true if the application would be approved given the current environment. */
    public boolean wouldApprove(LoanApplication application, EconomicEnvironment economy) {
        // Clone won't exist here, so we just replicate the logic read-only
        boolean stressed = economy.isStressed();
        double minScore = stressed ? MIN_CREDIT_SCORE_STRESSED : MIN_CREDIT_SCORE_NORMAL;
        double maxDti = stressed ? MAX_DTI_STRESSED : MAX_DTI_NORMAL;

        if (application.getCreditScore() < minScore) return false;

        double projectedRate = RateModel.loanRate(economy, application.getType());
        projectedRate = RateModel.adjustForCreditScore(projectedRate, application.getCreditScore());
        double projectedPayment = MathUtils.monthlyPayment(
                application.getRequestedAmount(), projectedRate, application.getRequestedTermMonths());
        double dti = MathUtils.debtToIncome(
                application.getExistingMonthlyDebtPayments() + projectedPayment,
                application.getMonthlyIncome());
        if (dti > maxDti) return false;

        double maxAmount = application.getMonthlyIncome() * 12 * 5;
        return application.getRequestedAmount() <= maxAmount;
    }
}
