package com.boma.banksim.loan;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoanApplicationTest {

    private LoanApplication valid() {
        return new LoanApplication("APP1", "C1", LoanType.CONSUMER,
                10_000, 5_000, 0, 700, 60);
    }

    @Test
    void constructor_initialStatusIsPending() {
        assertTrue(valid().isPending());
    }

    @Test
    void constructor_blankApplicationId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new LoanApplication("", "C1", LoanType.CONSUMER,
                        10_000, 5_000, 0, 700, 60));
    }

    @Test
    void constructor_creditScoreBelow300_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new LoanApplication("APP1", "C1", LoanType.CONSUMER,
                        10_000, 5_000, 0, 299, 60));
    }

    @Test
    void constructor_creditScoreAbove850_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new LoanApplication("APP1", "C1", LoanType.CONSUMER,
                        10_000, 5_000, 0, 851, 60));
    }

    @Test
    void constructor_zeroAmount_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new LoanApplication("APP1", "C1", LoanType.CONSUMER,
                        0, 5_000, 0, 700, 60));
    }

    @Test
    void constructor_zeroIncome_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new LoanApplication("APP1", "C1", LoanType.CONSUMER,
                        10_000, 0, 0, 700, 60));
    }

    @Test
    void constructor_zeroTerm_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new LoanApplication("APP1", "C1", LoanType.CONSUMER,
                        10_000, 5_000, 0, 700, 0));
    }

    @Test
    void constructor_negativeExistingDebt_clampedToZero() {
        LoanApplication app = new LoanApplication("APP1", "C1", LoanType.CONSUMER,
                10_000, 5_000, -500, 700, 60);
        assertEquals(0.0, app.getExistingMonthlyDebtPayments(), 0.001);
    }

    @Test
    void approve_setStatusToApproved() {
        LoanApplication app = valid();
        app.approve();
        assertTrue(app.isApproved());
        assertFalse(app.isPending());
    }

    @Test
    void reject_setsStatusToRejected() {
        LoanApplication app = valid();
        app.reject("Low score");
        assertTrue(app.isRejected());
        assertFalse(app.isPending());
    }

    @Test
    void reject_storesReason() {
        LoanApplication app = valid();
        app.reject("DTI too high");
        assertEquals("DTI too high", app.getRejectionReason());
    }

    @Test
    void approve_afterReject_throws() {
        LoanApplication app = valid();
        app.reject("reason");
        assertThrows(IllegalStateException.class, app::approve);
    }

    @Test
    void reject_afterApprove_throws() {
        LoanApplication app = valid();
        app.approve();
        assertThrows(IllegalStateException.class, () -> app.reject("reason"));
    }

    @Test
    void approve_twice_throws() {
        LoanApplication app = valid();
        app.approve();
        assertThrows(IllegalStateException.class, app::approve);
    }

    @Test
    void getRejectionReason_afterApprove_isNull() {
        LoanApplication app = valid();
        app.approve();
        assertNull(app.getRejectionReason());
    }
}
