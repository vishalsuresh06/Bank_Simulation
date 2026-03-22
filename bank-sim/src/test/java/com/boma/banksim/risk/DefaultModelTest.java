package com.boma.banksim.risk;

import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.loan.Loan;
import com.boma.banksim.loan.LoanStatus;
import com.boma.banksim.loan.LoanType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class DefaultModelTest {

    private DefaultModel model;
    private static final LocalDate DATE = LocalDate.of(2024, 1, 1);

    @BeforeEach
    void setUp() {
        model = new DefaultModel();
    }

    private Loan currentLoan() {
        return new Loan("LN1", "C1", LoanType.CONSUMER, 10_000, 0.08, 60, 202.76, DATE);
    }

    @Test
    void defaultProbability_currentLoan_normalEconomy() {
        Loan l = currentLoan();
        assertEquals(0.002, model.defaultProbability(l, EconomicEnvironment.normal()), 0.0001);
    }

    @Test
    void defaultProbability_lateLoan_normalEconomy() {
        Loan l = currentLoan();
        l.incrementDaysLate(); // LATE
        assertEquals(0.05, model.defaultProbability(l, EconomicEnvironment.normal()), 0.0001);
    }

    @Test
    void defaultProbability_delinquentLoan_normalEconomy() {
        Loan l = currentLoan();
        l.incrementDaysLate(); l.incrementDaysLate(); l.incrementDaysLate(); // DELINQUENT
        assertEquals(0.20, model.defaultProbability(l, EconomicEnvironment.normal()), 0.0001);
    }

    @Test
    void defaultProbability_defaultedLoan_normalEconomy() {
        Loan l = currentLoan();
        for (int i = 0; i < 6; i++) l.incrementDaysLate(); // DEFAULTED
        assertEquals(0.60, model.defaultProbability(l, EconomicEnvironment.normal()), 0.0001);
    }

    @Test
    void defaultProbability_currentLoan_expansionEconomy() {
        Loan l = currentLoan();
        assertEquals(0.0014, model.defaultProbability(l, EconomicEnvironment.expansion()), 0.0001);
    }

    @Test
    void defaultProbability_currentLoan_recessionEconomy() {
        Loan l = currentLoan();
        assertEquals(0.003, model.defaultProbability(l, EconomicEnvironment.recession()), 0.0001);
    }

    @Test
    void defaultProbability_delinquentLoan_recessionEconomy() {
        Loan l = currentLoan();
        for (int i = 0; i < 3; i++) l.incrementDaysLate(); // DELINQUENT
        assertEquals(0.30, model.defaultProbability(l, EconomicEnvironment.recession()), 0.001);
    }

    @Test
    void defaultProbability_defaultedLoan_crisisEconomy_cappedAt1() {
        Loan l = currentLoan();
        for (int i = 0; i < 6; i++) l.incrementDaysLate(); // DEFAULTED
        // 0.60 * 2.5 = 1.5 → clamped to 1.0
        assertEquals(1.0, model.defaultProbability(l, EconomicEnvironment.crisis()), 0.0001);
    }

    @Test
    void defaultProbability_chargedOff_returnsZero() {
        Loan l = currentLoan();
        l.chargeOff();
        assertEquals(0.0, model.defaultProbability(l, EconomicEnvironment.normal()), 0.0001);
    }

    @Test
    void defaultProbability_closed_returnsZero() {
        Loan l = currentLoan();
        l.applyPayment(20_000); // payoff
        assertEquals(LoanStatus.CLOSED, l.getStatus());
        assertEquals(0.0, model.defaultProbability(l, EconomicEnvironment.normal()), 0.0001);
    }

    @Test
    void shouldDefault_randomBelowProbability_returnsTrue() {
        Loan l = currentLoan();
        l.incrementDaysLate(); // LATE → prob=0.05
        assertTrue(model.shouldDefault(l, EconomicEnvironment.normal(), 0.03));
    }

    @Test
    void shouldDefault_randomAboveProbability_returnsFalse() {
        Loan l = currentLoan();
        l.incrementDaysLate(); // LATE → prob=0.05
        assertFalse(model.shouldDefault(l, EconomicEnvironment.normal(), 0.10));
    }

    @Test
    void shouldDefault_randomExactlyAtProbability_returnsFalse() {
        Loan l = currentLoan();
        l.incrementDaysLate(); // LATE → prob=0.05
        assertFalse(model.shouldDefault(l, EconomicEnvironment.normal(), 0.05));
    }
}
