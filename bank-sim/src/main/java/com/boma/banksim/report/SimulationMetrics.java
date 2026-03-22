package com.boma.banksim.report;

import com.boma.banksim.simulation.SimulationStepResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Collects and aggregates {@link SimulationStepResult} records produced
 * at the end of each simulation month.
 */
public class SimulationMetrics {

    private final String scenarioName;
    private final List<SimulationStepResult> steps;

    public SimulationMetrics(String scenarioName) {
        this.scenarioName = scenarioName;
        this.steps = new ArrayList<>();
    }

    public void record(SimulationStepResult result) {
        if (result != null) steps.add(result);
    }

    public List<SimulationStepResult> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public int size() { return steps.size(); }

    public SimulationStepResult getStep(int index) {
        return steps.get(index);
    }

    public SimulationStepResult getLastStep() {
        if (steps.isEmpty()) return null;
        return steps.get(steps.size() - 1);
    }

    // ---- Aggregate stats ----

    public double totalNetIncome() {
        return steps.stream().mapToDouble(SimulationStepResult::getNetIncome).sum();
    }

    public double totalChargeOffs() {
        return steps.stream().mapToDouble(SimulationStepResult::getChargeOffLosses).sum();
    }

    public int totalLoansDefaulted() {
        return steps.stream().mapToInt(SimulationStepResult::getNumDefaulted).sum();
    }

    public double peakEquity() {
        return steps.stream().mapToDouble(SimulationStepResult::getEquity).max().orElse(0);
    }

    public double troughEquity() {
        return steps.stream().mapToDouble(SimulationStepResult::getEquity).min().orElse(0);
    }

    public double averageNetInterestMargin() {
        if (steps.isEmpty()) return 0;
        return steps.stream()
                .mapToDouble(s -> (s.getGrossInterestIncome() - s.getDepositInterestExpense()) /
                        Math.max(1, s.getTotalAssets()))
                .average()
                .orElse(0);
    }

    public String getScenarioName() { return scenarioName; }

    @Override
    public String toString() {
        return "SimulationMetrics{scenario='" + scenarioName + "', steps=" + steps.size() +
               ", totalNetIncome=" + String.format("%.2f", totalNetIncome()) + '}';
    }
}
