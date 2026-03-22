package com.boma.banksim.simulation;

import java.time.LocalDate;

/**
 * Tracks simulated time. The simulation advances one calendar month at a time.
 */
public class SimulationClock {

    private LocalDate currentDate;
    private int stepCount;

    public SimulationClock(LocalDate startDate) {
        if (startDate == null) throw new IllegalArgumentException("Start date cannot be null.");
        this.currentDate = startDate;
        this.stepCount = 0;
    }

    public SimulationClock() {
        this(LocalDate.of(2024, 1, 1));
    }

    /** Advances the clock by one calendar month and returns the new date. */
    public LocalDate advanceOneMonth() {
        currentDate = currentDate.plusMonths(1);
        stepCount++;
        return currentDate;
    }

    public LocalDate getCurrentDate() { return currentDate; }
    public int getYear() { return currentDate.getYear(); }
    public int getMonth() { return currentDate.getMonthValue(); }
    public int getStepCount() { return stepCount; }

    /** First day of the current month. */
    public LocalDate periodStart() {
        return currentDate.withDayOfMonth(1);
    }

    /** Last day of the current month. */
    public LocalDate periodEnd() {
        return currentDate.withDayOfMonth(currentDate.lengthOfMonth());
    }

    @Override
    public String toString() {
        return "SimulationClock{date=" + currentDate + ", step=" + stepCount + '}';
    }
}
