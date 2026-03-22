package com.boma.banksim.loan;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LoanPaymentScheduleTest {

    @Test
    void generate_producesCorrectNumberOfInstallments() {
        LoanPaymentSchedule sched = LoanPaymentSchedule.generate(100_000, 0.06, 360);
        assertEquals(360, sched.size());
    }

    @Test
    void generate_lastInstallmentBalanceIsZero() {
        LoanPaymentSchedule sched = LoanPaymentSchedule.generate(10_000, 0.05, 60);
        LoanPaymentSchedule.PaymentInstallment last = sched.getInstallment(60);
        assertEquals(0.0, last.remainingBalance(), 0.01);
    }

    @Test
    void generate_zeroRate_principalPortionsAreEqual() {
        LoanPaymentSchedule sched = LoanPaymentSchedule.generate(1_200, 0.0, 12);
        for (var inst : sched.getInstallments()) {
            assertEquals(100.0, inst.principalPortion(), 0.01);
        }
    }

    @Test
    void generate_principalSumsToOriginalPrincipal() {
        LoanPaymentSchedule sched = LoanPaymentSchedule.generate(50_000, 0.07, 120);
        double totalPrincipal = sched.getInstallments().stream()
                .mapToDouble(LoanPaymentSchedule.PaymentInstallment::principalPortion)
                .sum();
        assertEquals(50_000.0, totalPrincipal, 5.0); // allow small rounding
    }

    @Test
    void generate_interestPortionsDecreaseMostlyOverTime() {
        LoanPaymentSchedule sched = LoanPaymentSchedule.generate(100_000, 0.06, 12);
        double firstInterest = sched.getInstallment(1).interestPortion();
        double lastInterest = sched.getInstallment(12).interestPortion();
        assertTrue(firstInterest > lastInterest,
                "First interest (" + firstInterest + ") should be > last (" + lastInterest + ")");
    }

    @Test
    void generate_firstInstallmentHasCorrectInterest() {
        // $120_000 at 6% annual → monthly rate 0.5% → first interest = 600
        LoanPaymentSchedule sched = LoanPaymentSchedule.generate(120_000, 0.06, 360);
        assertEquals(600.0, sched.getInstallment(1).interestPortion(), 0.01);
    }

    @Test
    void generate_negativePrincipal_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> LoanPaymentSchedule.generate(-1, 0.05, 60));
    }

    @Test
    void generate_negativeRate_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> LoanPaymentSchedule.generate(10_000, -0.01, 60));
    }

    @Test
    void generate_zeroTerm_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> LoanPaymentSchedule.generate(10_000, 0.05, 0));
    }

    @Test
    void getInstallment_outOfRangeLow_throws() {
        LoanPaymentSchedule sched = LoanPaymentSchedule.generate(10_000, 0.05, 12);
        assertThrows(IllegalArgumentException.class, () -> sched.getInstallment(0));
    }

    @Test
    void getInstallment_outOfRangeHigh_throws() {
        LoanPaymentSchedule sched = LoanPaymentSchedule.generate(10_000, 0.05, 12);
        assertThrows(IllegalArgumentException.class, () -> sched.getInstallment(13));
    }

    @Test
    void totalInterestCost_positiveForPositiveRate() {
        LoanPaymentSchedule sched = LoanPaymentSchedule.generate(10_000, 0.05, 12);
        assertTrue(sched.totalInterestCost() > 0);
    }

    @Test
    void totalInterestCost_zeroForZeroRate() {
        LoanPaymentSchedule sched = LoanPaymentSchedule.generate(10_000, 0.0, 12);
        assertEquals(0.0, sched.totalInterestCost(), 0.01);
    }

    @Test
    void monthNumbers_sequentialStartingAtOne() {
        LoanPaymentSchedule sched = LoanPaymentSchedule.generate(5_000, 0.06, 6);
        List<LoanPaymentSchedule.PaymentInstallment> insts = sched.getInstallments();
        for (int i = 0; i < insts.size(); i++) {
            assertEquals(i + 1, insts.get(i).month());
        }
    }
}
