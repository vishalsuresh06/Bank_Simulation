package com.boma.banksim.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void daysBetween_sameDay_returnsZero() {
        LocalDate d = LocalDate.of(2024, 1, 15);
        assertEquals(0, DateUtils.daysBetween(d, d));
    }

    @Test
    void daysBetween_oneDay() {
        assertEquals(1, DateUtils.daysBetween(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2)));
    }

    @Test
    void daysBetween_oneYear_standardYear() {
        long days = DateUtils.daysBetween(
                LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1));
        assertEquals(365, days);
    }

    @Test
    void daysBetween_oneLeapYear() {
        long days = DateUtils.daysBetween(
                LocalDate.of(2024, 1, 1), LocalDate.of(2025, 1, 1));
        assertEquals(366, days);
    }

    @Test
    void monthsBetween_sameMonth_returnsZero() {
        LocalDate d = LocalDate.of(2024, 6, 1);
        assertEquals(0, DateUtils.monthsBetween(d, d));
    }

    @Test
    void monthsBetween_threeMonths() {
        assertEquals(3, DateUtils.monthsBetween(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 4, 1)));
    }

    @Test
    void monthsBetween_oneYear() {
        assertEquals(12, DateUtils.monthsBetween(
                LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1)));
    }

    @Test
    void firstDayOfMonth_correctDate() {
        assertEquals(LocalDate.of(2024, 3, 1), DateUtils.firstDayOfMonth(2024, 3));
    }

    @Test
    void isAfterOrEqual_sameDate_returnsTrue() {
        LocalDate d = LocalDate.of(2024, 5, 10);
        assertTrue(DateUtils.isAfterOrEqual(d, d));
    }

    @Test
    void isAfterOrEqual_dateLater_returnsTrue() {
        assertTrue(DateUtils.isAfterOrEqual(
                LocalDate.of(2024, 5, 11), LocalDate.of(2024, 5, 10)));
    }

    @Test
    void isAfterOrEqual_dateEarlier_returnsFalse() {
        assertFalse(DateUtils.isAfterOrEqual(
                LocalDate.of(2024, 5, 9), LocalDate.of(2024, 5, 10)));
    }
}
