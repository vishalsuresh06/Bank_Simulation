package com.boma.banksim.simulation;

import java.time.LocalDate;

/**
 * A snapshot of the bank's financial position at the end of one simulation step (month).
 * Stored in {@link SimulationMetrics} and later exported to CSV.
 */
public class SimulationStepResult {

    private final LocalDate date;
    private final int step;

    // Balance sheet
    private final double reserves;
    private final double totalLoans;
    private final double totalDeposits;
    private final double equity;
    private final double totalAssets;

    // Income statement (period)
    private final double grossInterestIncome;
    private final double depositInterestExpense;
    private final double chargeOffLosses;
    private final double netIncome;

    // Loan book health
    private final int totalActiveLoans;
    private final int numDefaulted;
    private final int numChargedOff;
    private final int numNewLoans;

    // Economy
    private final double policyRate;
    private final double unemploymentRate;
    private final double depositorConfidence;

    public SimulationStepResult(
            LocalDate date, int step,
            double reserves, double totalLoans, double totalDeposits, double equity, double totalAssets,
            double grossInterestIncome, double depositInterestExpense, double chargeOffLosses, double netIncome,
            int totalActiveLoans, int numDefaulted, int numChargedOff, int numNewLoans,
            double policyRate, double unemploymentRate, double depositorConfidence
    ) {
        this.date = date;
        this.step = step;
        this.reserves = reserves;
        this.totalLoans = totalLoans;
        this.totalDeposits = totalDeposits;
        this.equity = equity;
        this.totalAssets = totalAssets;
        this.grossInterestIncome = grossInterestIncome;
        this.depositInterestExpense = depositInterestExpense;
        this.chargeOffLosses = chargeOffLosses;
        this.netIncome = netIncome;
        this.totalActiveLoans = totalActiveLoans;
        this.numDefaulted = numDefaulted;
        this.numChargedOff = numChargedOff;
        this.numNewLoans = numNewLoans;
        this.policyRate = policyRate;
        this.unemploymentRate = unemploymentRate;
        this.depositorConfidence = depositorConfidence;
    }

    /** CSV header matching the column order of {@link #toCsvRow()}. */
    public static String csvHeader() {
        return "step,date,reserves,totalLoans,totalDeposits,equity,totalAssets," +
               "grossInterestIncome,depositInterestExpense,chargeOffLosses,netIncome," +
               "totalActiveLoans,numDefaulted,numChargedOff,numNewLoans," +
               "policyRate,unemploymentRate,depositorConfidence";
    }

    public String toCsvRow() {
        return step + "," + date + "," +
               fmt(reserves) + "," + fmt(totalLoans) + "," + fmt(totalDeposits) + "," +
               fmt(equity) + "," + fmt(totalAssets) + "," +
               fmt(grossInterestIncome) + "," + fmt(depositInterestExpense) + "," +
               fmt(chargeOffLosses) + "," + fmt(netIncome) + "," +
               totalActiveLoans + "," + numDefaulted + "," + numChargedOff + "," + numNewLoans + "," +
               fmt(policyRate) + "," + fmt(unemploymentRate) + "," + fmt(depositorConfidence);
    }

    private static String fmt(double v) {
        return String.format("%.2f", v);
    }

    // ---- Getters ----

    public LocalDate getDate() { return date; }
    public int getStep() { return step; }
    public double getReserves() { return reserves; }
    public double getTotalLoans() { return totalLoans; }
    public double getTotalDeposits() { return totalDeposits; }
    public double getEquity() { return equity; }
    public double getTotalAssets() { return totalAssets; }
    public double getGrossInterestIncome() { return grossInterestIncome; }
    public double getDepositInterestExpense() { return depositInterestExpense; }
    public double getChargeOffLosses() { return chargeOffLosses; }
    public double getNetIncome() { return netIncome; }
    public int getTotalActiveLoans() { return totalActiveLoans; }
    public int getNumDefaulted() { return numDefaulted; }
    public int getNumChargedOff() { return numChargedOff; }
    public int getNumNewLoans() { return numNewLoans; }
    public double getPolicyRate() { return policyRate; }
    public double getUnemploymentRate() { return unemploymentRate; }
    public double getDepositorConfidence() { return depositorConfidence; }

    @Override
    public String toString() {
        return "Step " + step + " [" + date + "] equity=" + fmt(equity) +
               " NII=" + fmt(grossInterestIncome - depositInterestExpense) +
               " defaults=" + numDefaulted;
    }
}
