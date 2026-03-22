package com.boma.banksim.loan;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class LoanTest {

    private static final LocalDate DATE = LocalDate.of(2024, 1, 1);

    private Loan loan(double principal) {
        return new Loan("LN1", "C1", LoanType.CONSUMER, principal,
                0.12, 12, 88.85, DATE); // ~$88.85/month for $1000 at 12% for 12mo
    }

    private Loan simpleLoan() {
        return loan(1_000);
    }

    @Test
    void constructor_nullLoanId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Loan(null, "C1", LoanType.CONSUMER, 1_000, 0.12, 12, 88.85, DATE));
    }

    @Test
    void constructor_zeroPrincipal_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Loan("LN1", "C1", LoanType.CONSUMER, 0, 0.12, 12, 88.85, DATE));
    }

    @Test
    void constructor_zeroTerm_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Loan("LN1", "C1", LoanType.CONSUMER, 1_000, 0.12, 0, 88.85, DATE));
    }

    @Test
    void constructor_negativeRate_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Loan("LN1", "C1", LoanType.CONSUMER, 1_000, -0.01, 12, 88.85, DATE));
    }

    @Test
    void constructor_nullType_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Loan("LN1", "C1", null, 1_000, 0.12, 12, 88.85, DATE));
    }

    @Test
    void constructor_initialStatusIsCurrent() {
        assertEquals(LoanStatus.CURRENT, simpleLoan().getStatus());
    }

    @Test
    void constructor_initialDaysLateIsZero() {
        assertEquals(0, simpleLoan().getDaysLate());
    }

    @Test
    void constructor_outstandingEqualsOriginPrincipal() {
        assertEquals(1_000.0, simpleLoan().getOutstandingBalance(), 0.001);
    }

    @Test
    void applyPayment_reducesOutstandingBalance() {
        Loan l = simpleLoan();
        double before = l.getOutstandingBalance();
        l.applyPayment(88.85);
        assertTrue(l.getOutstandingBalance() < before);
    }

    @Test
    void applyPayment_returnsInterestPortion() {
        Loan l = simpleLoan();
        // Monthly rate = 1%, interest on $1000 = $10
        double interest = l.applyPayment(88.85);
        assertEquals(10.0, interest, 0.01);
    }

    @Test
    void applyPayment_principalPortionReducesBalance() {
        Loan l = simpleLoan();
        // Payment=88.85, interest=10 → principal=78.85
        l.applyPayment(88.85);
        assertEquals(1_000 - 78.85, l.getOutstandingBalance(), 0.02);
    }

    @Test
    void applyPayment_interestOnlyPayment_outstandingUnchanged() {
        Loan l = simpleLoan();
        // interest only = 10
        l.applyPayment(10.0);
        assertEquals(1_000.0, l.getOutstandingBalance(), 0.001);
    }

    @Test
    void applyPayment_fullPayoff_closesLoan() {
        Loan l = simpleLoan();
        // Pay more than balance + interest
        l.applyPayment(1_500);
        assertEquals(LoanStatus.CLOSED, l.getStatus());
        assertEquals(0.0, l.getOutstandingBalance(), 0.001);
    }

    @Test
    void applyPayment_lateStatus_resetsOnPayment() {
        Loan l = simpleLoan();
        l.incrementDaysLate();
        assertEquals(LoanStatus.LATE, l.getStatus());
        l.applyPayment(88.85);
        assertEquals(LoanStatus.CURRENT, l.getStatus());
        assertEquals(0, l.getDaysLate());
    }

    @Test
    void applyPayment_zero_throws() {
        assertThrows(IllegalArgumentException.class, () -> simpleLoan().applyPayment(0));
    }

    @Test
    void applyPayment_onChargedOff_throws() {
        Loan l = simpleLoan();
        l.chargeOff();
        assertThrows(IllegalStateException.class, () -> l.applyPayment(100));
    }

    @Test
    void applyPayment_onClosed_throws() {
        Loan l = simpleLoan();
        l.applyPayment(2_000); // close it
        assertThrows(IllegalStateException.class, () -> l.applyPayment(100));
    }

    @Test
    void incrementDaysLate_once_statusLate() {
        Loan l = simpleLoan();
        l.incrementDaysLate();
        assertEquals(LoanStatus.LATE, l.getStatus());
        assertEquals(30, l.getDaysLate());
    }

    @Test
    void incrementDaysLate_threeTimes_statusDelinquent() {
        Loan l = simpleLoan();
        l.incrementDaysLate();
        l.incrementDaysLate();
        l.incrementDaysLate();
        assertEquals(LoanStatus.DELINQUENT, l.getStatus());
        assertEquals(90, l.getDaysLate());
    }

    @Test
    void incrementDaysLate_sixTimes_statusDefaulted() {
        Loan l = simpleLoan();
        for (int i = 0; i < 6; i++) l.incrementDaysLate();
        assertEquals(LoanStatus.DEFAULTED, l.getStatus());
        assertEquals(180, l.getDaysLate());
    }

    @Test
    void incrementDaysLate_onChargedOff_noOp() {
        Loan l = simpleLoan();
        l.chargeOff();
        int daysBefore = l.getDaysLate();
        l.incrementDaysLate(); // should be no-op
        assertEquals(daysBefore, l.getDaysLate());
    }

    @Test
    void chargeOff_returnsOutstandingBalance() {
        Loan l = simpleLoan();
        double loss = l.chargeOff();
        assertEquals(1_000.0, loss, 0.001);
    }

    @Test
    void chargeOff_setsStatusToChargedOff() {
        Loan l = simpleLoan();
        l.chargeOff();
        assertEquals(LoanStatus.CHARGED_OFF, l.getStatus());
    }

    @Test
    void chargeOff_zerosOutstandingBalance() {
        Loan l = simpleLoan();
        l.chargeOff();
        assertEquals(0.0, l.getOutstandingBalance(), 0.001);
    }

    @Test
    void chargeOff_alreadyChargedOff_throws() {
        Loan l = simpleLoan();
        l.chargeOff();
        assertThrows(IllegalStateException.class, l::chargeOff);
    }

    @Test
    void isActive_currentStatusIsActive() {
        assertTrue(simpleLoan().isActive());
    }

    @Test
    void isActive_lateStatusIsActive() {
        Loan l = simpleLoan();
        l.incrementDaysLate();
        assertTrue(l.isActive());
    }

    @Test
    void isActive_delinquentIsActive() {
        Loan l = simpleLoan();
        for (int i = 0; i < 3; i++) l.incrementDaysLate();
        assertTrue(l.isActive());
    }

    @Test
    void isActive_defaultedNotActive() {
        Loan l = simpleLoan();
        for (int i = 0; i < 6; i++) l.incrementDaysLate();
        assertFalse(l.isActive());
    }

    @Test
    void isDelinquent_delinquentStatus_true() {
        Loan l = simpleLoan();
        for (int i = 0; i < 3; i++) l.incrementDaysLate();
        assertTrue(l.isDelinquent());
    }

    @Test
    void isDelinquent_defaultedStatus_true() {
        Loan l = simpleLoan();
        for (int i = 0; i < 6; i++) l.incrementDaysLate();
        assertTrue(l.isDelinquent());
    }

    @Test
    void isDelinquent_currentStatus_false() {
        assertFalse(simpleLoan().isDelinquent());
    }
}
