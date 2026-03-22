package com.boma.banksim.economy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EconomicEnvironmentTest {

    @Test
    void constructor_negativePolicyRate_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new EconomicEnvironment(-0.01, 0.02, 0.04, EconomicState.NORMAL, 0.95));
    }

    @Test
    void constructor_inflationAboveOne_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new EconomicEnvironment(0.05, 1.1, 0.04, EconomicState.NORMAL, 0.95));
    }

    @Test
    void constructor_unemploymentAboveOne_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new EconomicEnvironment(0.05, 0.02, 1.1, EconomicState.NORMAL, 0.95));
    }

    @Test
    void constructor_confidenceAboveOne_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new EconomicEnvironment(0.05, 0.02, 0.04, EconomicState.NORMAL, 1.01));
    }

    @Test
    void constructor_confidenceBelowZero_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new EconomicEnvironment(0.05, 0.02, 0.04, EconomicState.NORMAL, -0.01));
    }

    @Test
    void constructor_nullState_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new EconomicEnvironment(0.05, 0.02, 0.04, null, 0.95));
    }

    @Test
    void normal_correctValues() {
        EconomicEnvironment e = EconomicEnvironment.normal();
        assertEquals(EconomicState.NORMAL, e.getState());
        assertEquals(0.95, e.getDepositorConfidence(), 0.001);
        assertFalse(e.isStressed());
    }

    @Test
    void recession_correctValues() {
        EconomicEnvironment e = EconomicEnvironment.recession();
        assertEquals(EconomicState.RECESSION, e.getState());
        assertEquals(0.75, e.getDepositorConfidence(), 0.001);
        assertTrue(e.isStressed());
    }

    @Test
    void expansion_notStressed() {
        assertFalse(EconomicEnvironment.expansion().isStressed());
    }

    @Test
    void crisis_isStressed() {
        assertTrue(EconomicEnvironment.crisis().isStressed());
    }

    @Test
    void isStressed_normalState_false() {
        assertFalse(EconomicEnvironment.normal().isStressed());
    }

    @Test
    void isStressed_recessionState_true() {
        assertTrue(EconomicEnvironment.recession().isStressed());
    }

    @Test
    void applyRateChange_positive_increases() {
        EconomicEnvironment e = EconomicEnvironment.normal(); // rate=0.05
        e.applyRateChange(+0.02);
        assertEquals(0.07, e.getPolicyRate(), 0.0001);
    }

    @Test
    void applyRateChange_negative_decreases() {
        EconomicEnvironment e = EconomicEnvironment.normal(); // rate=0.05
        e.applyRateChange(-0.02);
        assertEquals(0.03, e.getPolicyRate(), 0.0001);
    }

    @Test
    void applyRateChange_flooredAtZero() {
        EconomicEnvironment e = EconomicEnvironment.normal(); // rate=0.05
        e.applyRateChange(-0.10);
        assertEquals(0.0, e.getPolicyRate(), 0.0001);
    }

    @Test
    void applyUnemploymentChange_clampedTo1() {
        EconomicEnvironment e = EconomicEnvironment.normal(); // unemployment=0.04
        e.applyUnemploymentChange(+2.0);
        assertEquals(1.0, e.getUnemploymentRate(), 0.0001);
    }

    @Test
    void applyUnemploymentChange_clampedToZero() {
        EconomicEnvironment e = EconomicEnvironment.normal();
        e.applyUnemploymentChange(-1.0);
        assertEquals(0.0, e.getUnemploymentRate(), 0.0001);
    }

    @Test
    void applyConfidenceChange_clampedTo1() {
        EconomicEnvironment e = EconomicEnvironment.expansion(); // confidence=0.98
        e.applyConfidenceChange(+0.5);
        assertEquals(1.0, e.getDepositorConfidence(), 0.0001);
    }

    @Test
    void applyConfidenceChange_clampedTo0() {
        EconomicEnvironment e = EconomicEnvironment.crisis(); // confidence=0.50
        e.applyConfidenceChange(-2.0);
        assertEquals(0.0, e.getDepositorConfidence(), 0.0001);
    }

    @Test
    void setState_null_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> EconomicEnvironment.normal().setState(null));
    }

    @Test
    void setState_updatesState() {
        EconomicEnvironment e = EconomicEnvironment.normal();
        e.setState(EconomicState.RECESSION);
        assertEquals(EconomicState.RECESSION, e.getState());
    }
}
