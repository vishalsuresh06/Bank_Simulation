package com.boma.banksim.simulation;

import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.economy.MarketShock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration for a simulation run. Built with a fluent Builder.
 * Two preset factories are provided: {@link #baseline()} and {@link #recession()}.
 */
public class Scenario {

    private final String name;
    private final int durationMonths;
    private final LocalDate startDate;
    private final double initialReserves;
    private final double initialEquity;
    private final int numCustomers;
    private final EconomicEnvironment economy;
    private final List<MarketShock> shocks;
    private final long randomSeed;

    private Scenario(Builder b) {
        this.name = b.name;
        this.durationMonths = b.durationMonths;
        this.startDate = b.startDate;
        this.initialReserves = b.initialReserves;
        this.initialEquity = b.initialEquity;
        this.numCustomers = b.numCustomers;
        this.economy = b.economy;
        this.shocks = Collections.unmodifiableList(b.shocks);
        this.randomSeed = b.randomSeed;
    }

    // ---- Preset scenarios ----

    /** 12-month run in a stable economy with 200 customers. */
    public static Scenario baseline() {
        return new Builder("Baseline")
                .durationMonths(12)
                .startDate(LocalDate.of(2024, 1, 1))
                .initialReserves(5_000_000)
                .initialEquity(5_000_000)
                .numCustomers(200)
                .economy(EconomicEnvironment.normal())
                .randomSeed(42L)
                .build();
    }

    /**
     * 24-month run that starts normally, then a recession hits at month 6,
     * with partial recovery beginning at month 18.
     */
    public static Scenario recession() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        return new Builder("Recession")
                .durationMonths(24)
                .startDate(start)
                .initialReserves(5_000_000)
                .initialEquity(5_000_000)
                .numCustomers(200)
                .economy(EconomicEnvironment.normal())
                .addShock(MarketShock.recessionShock(start.plusMonths(6)))
                .addShock(MarketShock.recoveryShock(start.plusMonths(18)))
                .randomSeed(99L)
                .build();
    }

    // ---- Getters ----

    public String getName() { return name; }
    public int getDurationMonths() { return durationMonths; }
    public LocalDate getStartDate() { return startDate; }
    public double getInitialReserves() { return initialReserves; }
    public double getInitialEquity() { return initialEquity; }
    public int getNumCustomers() { return numCustomers; }
    public EconomicEnvironment getEconomy() { return economy; }
    public List<MarketShock> getShocks() { return shocks; }
    public long getRandomSeed() { return randomSeed; }

    // ---- Builder ----

    public static class Builder {
        private final String name;
        private int durationMonths = 12;
        private LocalDate startDate = LocalDate.of(2024, 1, 1);
        private double initialReserves = 1_000_000;
        private double initialEquity = 1_000_000;
        private int numCustomers = 100;
        private EconomicEnvironment economy = EconomicEnvironment.normal();
        private final List<MarketShock> shocks = new ArrayList<>();
        private long randomSeed = 42L;

        public Builder(String name) { this.name = name; }

        public Builder durationMonths(int v) { this.durationMonths = v; return this; }
        public Builder startDate(LocalDate v) { this.startDate = v; return this; }
        public Builder initialReserves(double v) { this.initialReserves = v; return this; }
        public Builder initialEquity(double v) { this.initialEquity = v; return this; }
        public Builder numCustomers(int v) { this.numCustomers = v; return this; }
        public Builder economy(EconomicEnvironment v) { this.economy = v; return this; }
        public Builder addShock(MarketShock s) { this.shocks.add(s); return this; }
        public Builder randomSeed(long v) { this.randomSeed = v; return this; }

        public Scenario build() { return new Scenario(this); }
    }

    @Override
    public String toString() {
        return "Scenario{name='" + name + "', months=" + durationMonths +
               ", customers=" + numCustomers + ", shocks=" + shocks.size() + '}';
    }
}
