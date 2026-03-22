package com.boma.banksim.report;

import com.boma.banksim.simulation.SimulationStepResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class SimulationMetricsTest {

    private SimulationMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new SimulationMetrics("TestScenario");
    }

    private SimulationStepResult step(int n, double equity, double netIncome, double chargeOffs) {
        return new SimulationStepResult(
                LocalDate.of(2024, 1, 1).plusMonths(n), n,
                1_000_000, 500_000, 800_000, equity, 1_500_000,
                netIncome + chargeOffs, chargeOffs, chargeOffs, netIncome,
                10, 0, (chargeOffs > 0 ? 1 : 0), 5,
                0.05, 0.04, 0.95
        );
    }

    @Test
    void newMetrics_isEmpty() {
        assertEquals(0, metrics.size());
    }

    @Test
    void record_null_ignoredSilently() {
        metrics.record(null);
        assertEquals(0, metrics.size());
    }

    @Test
    void record_addsStep() {
        metrics.record(step(1, 500_000, 1_000, 0));
        assertEquals(1, metrics.size());
    }

    @Test
    void getLastStep_emptyMetrics_returnsNull() {
        assertNull(metrics.getLastStep());
    }

    @Test
    void getLastStep_returnsLastAdded() {
        metrics.record(step(1, 500_000, 1_000, 0));
        metrics.record(step(2, 501_000, 1_100, 0));
        assertEquals(2, metrics.getLastStep().getStep());
    }

    @Test
    void getStep_returnsCorrectStep() {
        metrics.record(step(1, 500_000, 1_000, 0));
        metrics.record(step(2, 501_000, 1_100, 0));
        assertEquals(1, metrics.getStep(0).getStep());
    }

    @Test
    void getScenarioName_returnsConstructedName() {
        assertEquals("TestScenario", metrics.getScenarioName());
    }

    @Test
    void totalNetIncome_sumsAllSteps() {
        metrics.record(step(1, 500_000, 1_000, 0));
        metrics.record(step(2, 501_000, 2_000, 0));
        metrics.record(step(3, 502_000, 3_000, 0));
        assertEquals(6_000, metrics.totalNetIncome(), 0.01);
    }

    @Test
    void totalChargeOffs_sumsAllSteps() {
        metrics.record(step(1, 500_000, 1_000, 5_000));
        metrics.record(step(2, 501_000, 1_000, 2_000));
        assertEquals(7_000, metrics.totalChargeOffs(), 0.01);
    }

    @Test
    void peakEquity_returnsMaxEquity() {
        metrics.record(step(1, 400_000, 1_000, 0));
        metrics.record(step(2, 600_000, 1_000, 0));
        metrics.record(step(3, 500_000, 1_000, 0));
        assertEquals(600_000, metrics.peakEquity(), 0.01);
    }

    @Test
    void troughEquity_returnsMinEquity() {
        metrics.record(step(1, 400_000, 1_000, 0));
        metrics.record(step(2, 600_000, 1_000, 0));
        metrics.record(step(3, 300_000, 1_000, 0));
        assertEquals(300_000, metrics.troughEquity(), 0.01);
    }

    @Test
    void averageNetInterestMargin_emptyMetrics_returnsZero() {
        assertEquals(0.0, metrics.averageNetInterestMargin(), 0.0001);
    }

    @Test
    void getSteps_returnsAllRecordedSteps() {
        metrics.record(step(1, 500_000, 1_000, 0));
        metrics.record(step(2, 501_000, 1_100, 0));
        assertEquals(2, metrics.getSteps().size());
    }
}
