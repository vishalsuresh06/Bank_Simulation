package com.boma.banksim.economy;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class MarketShockTest {

    private static final LocalDate DATE = LocalDate.of(2024, 6, 1);

    @Test
    void recessionShock_changesStateToRecession() {
        EconomicEnvironment e = EconomicEnvironment.normal();
        MarketShock.recessionShock(DATE).apply(e);
        assertEquals(EconomicState.RECESSION, e.getState());
    }

    @Test
    void recessionShock_increasesRate() {
        EconomicEnvironment e = EconomicEnvironment.normal(); // rate=0.05
        MarketShock.recessionShock(DATE).apply(e);
        assertTrue(e.getPolicyRate() > 0.05);
    }

    @Test
    void recessionShock_decreasesConfidence() {
        EconomicEnvironment e = EconomicEnvironment.normal(); // confidence=0.95
        MarketShock.recessionShock(DATE).apply(e);
        assertTrue(e.getDepositorConfidence() < 0.95);
    }

    @Test
    void recessionShock_increasesUnemployment() {
        EconomicEnvironment e = EconomicEnvironment.normal(); // unemployment=0.04
        MarketShock.recessionShock(DATE).apply(e);
        assertTrue(e.getUnemploymentRate() > 0.04);
    }

    @Test
    void recoveryShock_changesStateToExpansion() {
        EconomicEnvironment e = EconomicEnvironment.recession();
        MarketShock.recoveryShock(DATE).apply(e);
        assertEquals(EconomicState.EXPANSION, e.getState());
    }

    @Test
    void recoveryShock_decreasesRate() {
        EconomicEnvironment e = EconomicEnvironment.recession(); // rate=0.07
        MarketShock.recoveryShock(DATE).apply(e);
        assertTrue(e.getPolicyRate() < 0.07);
    }

    @Test
    void rateCutShock_decreasesRateBySpecifiedAmount() {
        EconomicEnvironment e = EconomicEnvironment.normal(); // rate=0.05
        MarketShock.rateCutShock(DATE, 0.02).apply(e);
        assertEquals(0.03, e.getPolicyRate(), 0.0001);
    }

    @Test
    void shockWithNoNewState_preservesOriginalState() {
        EconomicEnvironment e = EconomicEnvironment.normal();
        new MarketShock.Builder("no state", DATE)
                .rateChange(+0.01)
                .build()
                .apply(e);
        assertEquals(EconomicState.NORMAL, e.getState());
    }

    @Test
    void builder_allFieldsSet() {
        MarketShock shock = new MarketShock.Builder("test", DATE)
                .rateChange(0.01)
                .unemploymentChange(0.02)
                .confidenceChange(-0.10)
                .newState(EconomicState.RECESSION)
                .build();

        assertEquals("test", shock.getDescription());
        assertEquals(DATE, shock.getScheduledDate());
        assertEquals(0.01, shock.getRateChange(), 0.0001);
        assertEquals(0.02, shock.getUnemploymentChange(), 0.0001);
        assertEquals(-0.10, shock.getConfidenceChange(), 0.0001);
        assertEquals(EconomicState.RECESSION, shock.getNewState());
    }

    @Test
    void apply_rateCannotGoNegative() {
        EconomicEnvironment e = new EconomicEnvironment(0.01, 0.02, 0.04, EconomicState.NORMAL, 0.95);
        MarketShock cut = MarketShock.rateCutShock(DATE, 0.05); // would take rate to -0.04
        cut.apply(e);
        assertEquals(0.0, e.getPolicyRate(), 0.0001);
    }
}
