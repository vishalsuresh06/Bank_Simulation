package com.boma.banksim.risk;

import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.loan.Loan;
import com.boma.banksim.loan.LoanType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CreditRiskModelTest {

    private CreditRiskModel model;
    private static final LocalDate DATE = LocalDate.of(2024, 1, 1);

    @BeforeEach
    void setUp() {
        model = new CreditRiskModel();
    }

    private Loan loan(LoanType type, double principal) {
        return new Loan("LN1", "C1", type, principal, 0.08, 60, 202.76, DATE);
    }

    @Test
    void lossGivenDefault_mortgage_is25pct() {
        assertEquals(0.25, model.lossGivenDefault(loan(LoanType.MORTGAGE, 100_000)), 0.0001);
    }

    @Test
    void lossGivenDefault_consumer_is70pct() {
        assertEquals(0.70, model.lossGivenDefault(loan(LoanType.CONSUMER, 10_000)), 0.0001);
    }

    @Test
    void lossGivenDefault_business_is50pct() {
        assertEquals(0.50, model.lossGivenDefault(loan(LoanType.BUSINESS, 50_000)), 0.0001);
    }

    @Test
    void expectedLoss_currentConsumerLoan_normalEconomy() {
        Loan l = loan(LoanType.CONSUMER, 10_000);
        EconomicEnvironment e = EconomicEnvironment.normal();
        // PD=0.002, LGD=0.70, EAD=10000 → EL=14.0
        assertEquals(14.0, model.expectedLoss(l, e), 0.01);
    }

    @Test
    void expectedLoss_currentMortgage_normalEconomy() {
        Loan l = loan(LoanType.MORTGAGE, 100_000);
        EconomicEnvironment e = EconomicEnvironment.normal();
        // PD=0.002, LGD=0.25, EAD=100000 → EL=50.0
        assertEquals(50.0, model.expectedLoss(l, e), 0.01);
    }

    @Test
    void expectedLoss_delinquentLoan_higherThanCurrent() {
        Loan current = loan(LoanType.CONSUMER, 10_000);
        Loan delinquent = loan(LoanType.CONSUMER, 10_000);
        for (int i = 0; i < 3; i++) delinquent.incrementDaysLate();

        EconomicEnvironment e = EconomicEnvironment.normal();
        assertTrue(model.expectedLoss(delinquent, e) > model.expectedLoss(current, e));
    }

    @Test
    void totalExpectedLoss_sumsAllLoans() {
        List<Loan> loans = List.of(
                loan(LoanType.CONSUMER, 10_000),
                loan(LoanType.CONSUMER, 10_000)
        );
        EconomicEnvironment e = EconomicEnvironment.normal();
        assertEquals(28.0, model.totalExpectedLoss(loans, e), 0.01); // 14.0 * 2
    }

    @Test
    void totalExpectedLoss_emptyCollection_returnsZero() {
        assertEquals(0.0, model.totalExpectedLoss(List.of(), EconomicEnvironment.normal()), 0.0001);
    }

    @Test
    void expectedLoss_recessionEconomy_higherThanNormal() {
        Loan l = loan(LoanType.CONSUMER, 10_000);
        double normal = model.expectedLoss(l, EconomicEnvironment.normal());
        double recession = model.expectedLoss(l, EconomicEnvironment.recession());
        assertTrue(recession > normal);
    }
}
