package com.boma.banksim.customer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomerProfileTest {

    private CustomerProfile valid() {
        return new CustomerProfile(60_000, 0.6, 700, 0.3, 0.4);
    }

    @Test
    void constructor_validArgs_setsFields() {
        CustomerProfile p = valid();
        assertEquals(60_000, p.getIncome(), 0.01);
        assertEquals(0.6, p.getSpendingRate(), 0.001);
        assertEquals(700, p.getCreditScore(), 0.001);
        assertEquals(0.3, p.getWithdrawalSensitivity(), 0.001);
        assertEquals(0.4, p.getRateSensitivity(), 0.001);
    }

    @Test
    void constructor_creditScoreBelow300_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CustomerProfile(50_000, 0.5, 299, 0.2, 0.2));
    }

    @Test
    void constructor_creditScoreAbove850_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CustomerProfile(50_000, 0.5, 851, 0.2, 0.2));
    }

    @Test
    void constructor_creditScore300_allowed() {
        assertDoesNotThrow(() -> new CustomerProfile(50_000, 0.5, 300, 0.2, 0.2));
    }

    @Test
    void constructor_creditScore850_allowed() {
        assertDoesNotThrow(() -> new CustomerProfile(50_000, 0.5, 850, 0.2, 0.2));
    }

    @Test
    void constructor_negativeIncome_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CustomerProfile(-1, 0.5, 700, 0.2, 0.2));
    }

    @Test
    void constructor_withdrawalSensitivityAbove1_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CustomerProfile(50_000, 0.5, 700, 1.01, 0.2));
    }

    @Test
    void constructor_withdrawalSensitivityBelow0_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new CustomerProfile(50_000, 0.5, 700, -0.01, 0.2));
    }

    @Test
    void setIncome_negative_throws() {
        CustomerProfile p = valid();
        assertThrows(IllegalArgumentException.class, () -> p.setIncome(-1));
    }

    @Test
    void setIncome_zero_allowed() {
        CustomerProfile p = valid();
        assertDoesNotThrow(() -> p.setIncome(0));
    }

    @Test
    void setCreditScore_boundary_300and850_allowed() {
        CustomerProfile p = valid();
        assertDoesNotThrow(() -> p.setCreditScore(300));
        assertDoesNotThrow(() -> p.setCreditScore(850));
    }

    @Test
    void setCreditScore_outOfRange_throws() {
        CustomerProfile p = valid();
        assertThrows(IllegalArgumentException.class, () -> p.setCreditScore(200));
    }

    @Test
    void setWithdrawalSensitivity_validBoundaries_noException() {
        CustomerProfile p = valid();
        assertDoesNotThrow(() -> p.setWithdrawalSensitivity(0.0));
        assertDoesNotThrow(() -> p.setWithdrawalSensitivity(1.0));
    }

    @Test
    void setWithdrawalSensitivity_aboveOne_throws() {
        CustomerProfile p = valid();
        assertThrows(IllegalArgumentException.class, () -> p.setWithdrawalSensitivity(1.1));
    }
}
