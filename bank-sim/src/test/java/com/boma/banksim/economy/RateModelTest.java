package com.boma.banksim.economy;

import com.boma.banksim.loan.LoanType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateModelTest {

    @Test
    void loanRate_consumer_addsConsumerSpread() {
        // NORMAL: policyRate=0.05, consumer spread=0.03 → 0.08
        EconomicEnvironment e = EconomicEnvironment.normal();
        assertEquals(0.08, RateModel.loanRate(e, LoanType.CONSUMER), 0.0001);
    }

    @Test
    void loanRate_mortgage_addsMortgageSpread() {
        // NORMAL: 0.05 + 0.015 = 0.065
        EconomicEnvironment e = EconomicEnvironment.normal();
        assertEquals(0.065, RateModel.loanRate(e, LoanType.MORTGAGE), 0.0001);
    }

    @Test
    void loanRate_business_addsBusinessSpread() {
        // NORMAL: 0.05 + 0.025 = 0.075
        EconomicEnvironment e = EconomicEnvironment.normal();
        assertEquals(0.075, RateModel.loanRate(e, LoanType.BUSINESS), 0.0001);
    }

    @Test
    void loanRate_recessionEconomy_addsPremium() {
        EconomicEnvironment e = EconomicEnvironment.recession(); // rate=0.07
        double normalConsumer = 0.07 + 0.03; // no recession premium
        double stressedConsumer = RateModel.loanRate(e, LoanType.CONSUMER);
        assertEquals(normalConsumer + 0.01, stressedConsumer, 0.0001);
    }

    @Test
    void loanRate_greaterThanPolicyRate() {
        EconomicEnvironment e = EconomicEnvironment.normal();
        assertTrue(RateModel.loanRate(e, LoanType.MORTGAGE) > e.getPolicyRate());
    }

    @Test
    void savingsRate_normalEconomy_deductsMargin() {
        // policyRate=0.05, margin=0.015 → savings=0.035
        EconomicEnvironment e = EconomicEnvironment.normal();
        assertEquals(0.035, RateModel.savingsRate(e), 0.0001);
    }

    @Test
    void savingsRate_belowMargin_flooredAtZero() {
        // Low policy rate: below 1.5% → floor at 0
        EconomicEnvironment e = new EconomicEnvironment(0.01, 0.02, 0.04, EconomicState.NORMAL, 0.95);
        assertEquals(0.0, RateModel.savingsRate(e), 0.0001);
    }

    @Test
    void savingsRate_lessThanLoanRate() {
        EconomicEnvironment e = EconomicEnvironment.normal();
        assertTrue(RateModel.savingsRate(e) < RateModel.loanRate(e, LoanType.CONSUMER));
    }

    @Test
    void adjustForCreditScore_excellentScore_noAdjustment() {
        double base = 0.08;
        assertEquals(base, RateModel.adjustForCreditScore(base, 750), 0.0001);
        assertEquals(base, RateModel.adjustForCreditScore(base, 820), 0.0001);
    }

    @Test
    void adjustForCreditScore_score700to749_adds0_5pct() {
        assertEquals(0.085, RateModel.adjustForCreditScore(0.08, 720), 0.0001);
    }

    @Test
    void adjustForCreditScore_score650to699_adds1pct() {
        assertEquals(0.09, RateModel.adjustForCreditScore(0.08, 660), 0.0001);
    }

    @Test
    void adjustForCreditScore_score600to649_adds2pct() {
        assertEquals(0.10, RateModel.adjustForCreditScore(0.08, 620), 0.0001);
    }

    @Test
    void adjustForCreditScore_below600_adds3_5pct() {
        assertEquals(0.115, RateModel.adjustForCreditScore(0.08, 580), 0.0001);
    }

    @Test
    void adjustForCreditScore_rateMonotonicallyIncreases() {
        double base = 0.06;
        double r800 = RateModel.adjustForCreditScore(base, 800);
        double r700 = RateModel.adjustForCreditScore(base, 700);
        double r600 = RateModel.adjustForCreditScore(base, 600);
        double r550 = RateModel.adjustForCreditScore(base, 550);
        assertTrue(r800 <= r700 && r700 <= r600 && r600 <= r550);
    }
}
