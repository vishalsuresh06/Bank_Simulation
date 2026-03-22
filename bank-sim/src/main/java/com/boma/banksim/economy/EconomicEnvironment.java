package com.boma.banksim.economy;

/**
 * Models the macroeconomic environment the bank operates in.
 *
 * Key variables:
 *   policyRate          – central bank benchmark rate (e.g. 0.05 = 5%)
 *   inflation           – annual inflation rate (e.g. 0.03 = 3%)
 *   unemploymentRate    – fraction of labour force unemployed (e.g. 0.05 = 5%)
 *   state               – qualitative regime (NORMAL, EXPANSION, RECESSION, CRISIS)
 *   depositorConfidence – [0.0, 1.0]; lower values trigger stress withdrawals
 */
public class EconomicEnvironment {

    private double policyRate;
    private double inflation;
    private double unemploymentRate;
    private EconomicState state;
    private double depositorConfidence;

    public EconomicEnvironment(
            double policyRate,
            double inflation,
            double unemploymentRate,
            EconomicState state,
            double depositorConfidence
    ) {
        if (policyRate < 0) throw new IllegalArgumentException("Policy rate cannot be negative.");
        if (inflation < -1 || inflation > 1) throw new IllegalArgumentException("Inflation must be in [-1, 1].");
        if (unemploymentRate < 0 || unemploymentRate > 1) throw new IllegalArgumentException("Unemployment rate must be in [0, 1].");
        if (state == null) throw new IllegalArgumentException("State cannot be null.");
        if (depositorConfidence < 0 || depositorConfidence > 1) throw new IllegalArgumentException("Depositor confidence must be in [0, 1].");

        this.policyRate = policyRate;
        this.inflation = inflation;
        this.unemploymentRate = unemploymentRate;
        this.state = state;
        this.depositorConfidence = depositorConfidence;
    }

    // ---- Factory methods ----

    /** Typical stable economy: moderate rates, low unemployment, high confidence. */
    public static EconomicEnvironment normal() {
        return new EconomicEnvironment(0.05, 0.02, 0.04, EconomicState.NORMAL, 0.95);
    }

    /** Expansion: low rates, strong growth, tight labour market. */
    public static EconomicEnvironment expansion() {
        return new EconomicEnvironment(0.03, 0.03, 0.03, EconomicState.EXPANSION, 0.98);
    }

    /** Recession: elevated rates, rising unemployment, falling confidence. */
    public static EconomicEnvironment recession() {
        return new EconomicEnvironment(0.07, 0.04, 0.08, EconomicState.RECESSION, 0.75);
    }

    /** Severe stress: high rates, mass unemployment, near-panic confidence. */
    public static EconomicEnvironment crisis() {
        return new EconomicEnvironment(0.10, 0.06, 0.15, EconomicState.CRISIS, 0.50);
    }

    // ---- Mutators (used by MarketShock) ----

    public void applyRateChange(double delta) {
        this.policyRate = Math.max(0, policyRate + delta);
    }

    public void applyUnemploymentChange(double delta) {
        this.unemploymentRate = Math.max(0, Math.min(1, unemploymentRate + delta));
    }

    public void applyConfidenceChange(double delta) {
        this.depositorConfidence = Math.max(0, Math.min(1, depositorConfidence + delta));
    }

    public void setState(EconomicState state) {
        if (state == null) throw new IllegalArgumentException("State cannot be null.");
        this.state = state;
    }

    // ---- Getters ----

    public double getPolicyRate() { return policyRate; }
    public double getInflation() { return inflation; }
    public double getUnemploymentRate() { return unemploymentRate; }
    public EconomicState getState() { return state; }
    public double getDepositorConfidence() { return depositorConfidence; }

    public boolean isStressed() {
        return state == EconomicState.RECESSION || state == EconomicState.CRISIS;
    }

    @Override
    public String toString() {
        return "EconomicEnvironment{" +
                "state=" + state +
                ", policyRate=" + policyRate +
                ", inflation=" + inflation +
                ", unemployment=" + unemploymentRate +
                ", confidence=" + depositorConfidence +
                '}';
    }
}
