package com.boma.banksim.simulation;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class SimulationClockTest {

    @Test
    void constructor_nullDate_throws() {
        assertThrows(IllegalArgumentException.class, () -> new SimulationClock(null));
    }

    @Test
    void defaultConstructor_startsAt2024Jan1() {
        SimulationClock clock = new SimulationClock();
        assertEquals(LocalDate.of(2024, 1, 1), clock.getCurrentDate());
    }

    @Test
    void customConstructor_setsStartDate() {
        LocalDate start = LocalDate.of(2023, 6, 15);
        SimulationClock clock = new SimulationClock(start);
        assertEquals(start, clock.getCurrentDate());
    }

    @Test
    void advanceOneMonth_returnsNextMonth() {
        SimulationClock clock = new SimulationClock(LocalDate.of(2024, 1, 1));
        LocalDate next = clock.advanceOneMonth();
        assertEquals(LocalDate.of(2024, 2, 1), next);
    }

    @Test
    void advanceOneMonth_incrementsStepCount() {
        SimulationClock clock = new SimulationClock();
        assertEquals(0, clock.getStepCount());
        clock.advanceOneMonth();
        assertEquals(1, clock.getStepCount());
    }

    @Test
    void advanceOneMonth_acrossYearBoundary() {
        SimulationClock clock = new SimulationClock(LocalDate.of(2024, 12, 1));
        LocalDate next = clock.advanceOneMonth();
        assertEquals(LocalDate.of(2025, 1, 1), next);
    }

    @Test
    void advanceOneMonth_multipleAdvances_stepCountCorrect() {
        SimulationClock clock = new SimulationClock();
        for (int i = 0; i < 12; i++) clock.advanceOneMonth();
        assertEquals(12, clock.getStepCount());
    }

    @Test
    void getYear_returnsCurrentYear() {
        SimulationClock clock = new SimulationClock(LocalDate.of(2025, 3, 1));
        assertEquals(2025, clock.getYear());
    }

    @Test
    void getMonth_returnsCurrentMonth() {
        SimulationClock clock = new SimulationClock(LocalDate.of(2024, 7, 1));
        assertEquals(7, clock.getMonth());
    }

    @Test
    void periodStart_isFirstDayOfMonth() {
        SimulationClock clock = new SimulationClock(LocalDate.of(2024, 3, 15));
        assertEquals(1, clock.periodStart().getDayOfMonth());
        assertEquals(3, clock.periodStart().getMonthValue());
    }

    @Test
    void periodEnd_isLastDayOfMonth_standard() {
        SimulationClock clock = new SimulationClock(LocalDate.of(2024, 1, 1));
        assertEquals(31, clock.periodEnd().getDayOfMonth());
    }

    @Test
    void periodEnd_isLastDayOfMonth_february_leapYear() {
        SimulationClock clock = new SimulationClock(LocalDate.of(2024, 2, 1)); // 2024 is leap
        assertEquals(29, clock.periodEnd().getDayOfMonth());
    }

    @Test
    void periodEnd_isLastDayOfMonth_february_nonLeapYear() {
        SimulationClock clock = new SimulationClock(LocalDate.of(2023, 2, 1));
        assertEquals(28, clock.periodEnd().getDayOfMonth());
    }
}
