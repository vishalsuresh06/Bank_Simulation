package com.boma.banksim.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    private DateUtils() {}

    public static long daysBetween(LocalDate from, LocalDate to) {
        return ChronoUnit.DAYS.between(from, to);
    }

    public static long monthsBetween(LocalDate from, LocalDate to) {
        return ChronoUnit.MONTHS.between(from, to);
    }

    public static LocalDate firstDayOfMonth(int year, int month) {
        return LocalDate.of(year, month, 1);
    }

    public static boolean isAfterOrEqual(LocalDate date, LocalDate reference) {
        return !date.isBefore(reference);
    }
}
