package com.boma.banksim.integration;

import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.economy.EconomicState;
import com.boma.banksim.economy.MarketShock;
import com.boma.banksim.simulation.EventQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests verifying EventQueue processes MarketShocks
 * at the correct scheduled dates.
 */
class MarketShockIntegrationTest {

    private EventQueue eventQueue;
    private EconomicEnvironment economy;

    @BeforeEach
    void setUp() {
        eventQueue = new EventQueue();
        economy = EconomicEnvironment.normal();
    }

    @Test
    void recessionShock_appliedOnScheduledDate_changesStateToRecession() {
        LocalDate shockDate = LocalDate.of(2024, 6, 1);
        MarketShock shock = MarketShock.recessionShock(shockDate);
        eventQueue.schedule(shockDate, shock.getDescription(), () -> shock.apply(economy));

        eventQueue.processUpTo(shockDate);

        assertEquals(EconomicState.RECESSION, economy.getState());
    }

    @Test
    void recessionShock_notApplied_beforeScheduledDate() {
        LocalDate shockDate = LocalDate.of(2024, 6, 1);
        MarketShock shock = MarketShock.recessionShock(shockDate);
        eventQueue.schedule(shockDate, shock.getDescription(), () -> shock.apply(economy));

        eventQueue.processUpTo(shockDate.minusDays(1));

        assertEquals(EconomicState.NORMAL, economy.getState());
    }

    @Test
    void recoveryShock_afterRecession_changesStateBack() {
        LocalDate recessionDate = LocalDate.of(2024, 3, 1);
        LocalDate recoveryDate = LocalDate.of(2024, 9, 1);

        MarketShock recession = MarketShock.recessionShock(recessionDate);
        MarketShock recovery = MarketShock.recoveryShock(recoveryDate);

        eventQueue.schedule(recessionDate, recession.getDescription(), () -> recession.apply(economy));
        eventQueue.schedule(recoveryDate, recovery.getDescription(), () -> recovery.apply(economy));

        // After recession, before recovery
        eventQueue.processUpTo(recessionDate);
        assertEquals(EconomicState.RECESSION, economy.getState());

        // After recovery — recoveryShock transitions to EXPANSION (partial recovery)
        eventQueue.processUpTo(recoveryDate);
        assertEquals(EconomicState.EXPANSION, economy.getState());
    }

    @Test
    void multipleShocks_executedInChronologicalOrder() {
        List<String> order = new ArrayList<>();

        LocalDate d1 = LocalDate.of(2024, 3, 1);
        LocalDate d2 = LocalDate.of(2024, 6, 1);
        LocalDate d3 = LocalDate.of(2024, 9, 1);

        // Schedule out of order
        eventQueue.schedule(d3, "third", () -> order.add("recovery"));
        eventQueue.schedule(d1, "first", () -> order.add("recession"));
        eventQueue.schedule(d2, "second", () -> order.add("crisis"));

        eventQueue.processUpTo(d3);

        assertEquals(List.of("recession", "crisis", "recovery"), order);
    }

    @Test
    void rateCutShock_reducesInterestRate() {
        double rateBefore = economy.getPolicyRate();
        LocalDate shockDate = LocalDate.of(2024, 4, 1);
        MarketShock shock = MarketShock.rateCutShock(shockDate, 0.01);
        eventQueue.schedule(shockDate, shock.getDescription(), () -> shock.apply(economy));

        eventQueue.processUpTo(shockDate);

        assertEquals(rateBefore - 0.01, economy.getPolicyRate(), 0.0001);
    }

    @Test
    void shockNotYetDue_economyUnchanged() {
        LocalDate futureDate = LocalDate.of(2025, 1, 1);
        MarketShock shock = MarketShock.recessionShock(futureDate);
        eventQueue.schedule(futureDate, shock.getDescription(), () -> shock.apply(economy));

        eventQueue.processUpTo(LocalDate.of(2024, 6, 1));

        assertEquals(EconomicState.NORMAL, economy.getState());
    }

    @Test
    void shockApplied_depositorConfidenceDecreases() {
        double confidenceBefore = economy.getDepositorConfidence();
        LocalDate shockDate = LocalDate.of(2024, 5, 1);
        MarketShock shock = MarketShock.recessionShock(shockDate);
        eventQueue.schedule(shockDate, shock.getDescription(), () -> shock.apply(economy));

        eventQueue.processUpTo(shockDate);

        assertTrue(economy.getDepositorConfidence() < confidenceBefore);
    }

    @Test
    void processedShock_removedFromQueue() {
        LocalDate shockDate = LocalDate.of(2024, 2, 1);
        MarketShock shock = MarketShock.recessionShock(shockDate);
        eventQueue.schedule(shockDate, shock.getDescription(), () -> shock.apply(economy));

        assertEquals(1, eventQueue.pendingCount());
        eventQueue.processUpTo(shockDate);
        assertEquals(0, eventQueue.pendingCount());
    }

    @Test
    void simulationMonthlyLoop_appliesShocksAtCorrectStep() {
        // Simulate 12 monthly steps, shock at step 6
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate shockDate = start.plusMonths(5); // 6th month = June

        MarketShock shock = MarketShock.recessionShock(shockDate);
        eventQueue.schedule(shockDate, shock.getDescription(), () -> shock.apply(economy));

        for (int step = 1; step <= 12; step++) {
            LocalDate currentDate = start.plusMonths(step - 1);
            eventQueue.processUpTo(currentDate);

            if (step < 6) {
                assertEquals(EconomicState.NORMAL, economy.getState(),
                        "Should be NORMAL at step " + step);
            } else {
                assertEquals(EconomicState.RECESSION, economy.getState(),
                        "Should be RECESSION at step " + step);
            }
        }
    }
}
