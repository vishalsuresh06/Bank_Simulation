package com.boma.banksim.economy;

import java.time.LocalDate;

/**
 * A scheduled event that abruptly changes macroeconomic parameters —
 * simulating rate hikes, recessions, banking panics, etc.
 *
 * Apply a shock via {@link #apply(EconomicEnvironment)}.
 */
public class MarketShock {

    private final String description;
    private final LocalDate scheduledDate;
    private final double rateChange;
    private final double unemploymentChange;
    private final double confidenceChange;
    private final EconomicState newState;

    private MarketShock(Builder builder) {
        this.description = builder.description;
        this.scheduledDate = builder.scheduledDate;
        this.rateChange = builder.rateChange;
        this.unemploymentChange = builder.unemploymentChange;
        this.confidenceChange = builder.confidenceChange;
        this.newState = builder.newState;
    }

    /** Applies this shock to the given economic environment. */
    public void apply(EconomicEnvironment economy) {
        economy.applyRateChange(rateChange);
        economy.applyUnemploymentChange(unemploymentChange);
        economy.applyConfidenceChange(confidenceChange);
        if (newState != null) {
            economy.setState(newState);
        }
    }

    public String getDescription() { return description; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public double getRateChange() { return rateChange; }
    public double getUnemploymentChange() { return unemploymentChange; }
    public double getConfidenceChange() { return confidenceChange; }
    public EconomicState getNewState() { return newState; }

    // ---- Preset shocks ----

    public static MarketShock recessionShock(LocalDate date) {
        return new Builder("Recession onset", date)
                .rateChange(+0.02)
                .unemploymentChange(+0.04)
                .confidenceChange(-0.20)
                .newState(EconomicState.RECESSION)
                .build();
    }

    public static MarketShock recoveryShock(LocalDate date) {
        return new Builder("Economic recovery", date)
                .rateChange(-0.01)
                .unemploymentChange(-0.02)
                .confidenceChange(+0.15)
                .newState(EconomicState.EXPANSION)
                .build();
    }

    public static MarketShock rateCutShock(LocalDate date, double cut) {
        return new Builder("Central bank rate cut", date)
                .rateChange(-cut)
                .build();
    }

    // ---- Builder ----

    public static class Builder {
        private final String description;
        private final LocalDate scheduledDate;
        private double rateChange = 0.0;
        private double unemploymentChange = 0.0;
        private double confidenceChange = 0.0;
        private EconomicState newState = null;

        public Builder(String description, LocalDate scheduledDate) {
            this.description = description;
            this.scheduledDate = scheduledDate;
        }

        public Builder rateChange(double v) { this.rateChange = v; return this; }
        public Builder unemploymentChange(double v) { this.unemploymentChange = v; return this; }
        public Builder confidenceChange(double v) { this.confidenceChange = v; return this; }
        public Builder newState(EconomicState s) { this.newState = s; return this; }
        public MarketShock build() { return new MarketShock(this); }
    }

    @Override
    public String toString() {
        return "MarketShock{'" + description + "' on " + scheduledDate + '}';
    }
}
