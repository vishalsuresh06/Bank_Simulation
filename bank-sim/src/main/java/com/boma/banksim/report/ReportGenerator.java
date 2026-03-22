package com.boma.banksim.report;

import com.boma.banksim.simulation.SimulationStepResult;

/**
 * Prints formatted simulation reports to standard output.
 */
public class ReportGenerator {

    public void printStepSummary(SimulationStepResult step) {
        System.out.printf(
                "[Step %3d | %s] Assets=%,12.0f | Equity=%,10.0f | NII=%,8.0f | Defaults=%d | Rate=%.1f%%%n",
                step.getStep(),
                step.getDate(),
                step.getTotalAssets(),
                step.getEquity(),
                step.getGrossInterestIncome() - step.getDepositInterestExpense(),
                step.getNumDefaulted(),
                step.getPolicyRate() * 100
        );
    }

    public void printFinalReport(SimulationMetrics metrics) {
        SimulationStepResult last = metrics.getLastStep();
        if (last == null) {
            System.out.println("No simulation data.");
            return;
        }

        System.out.println();
        System.out.println("========================================");
        System.out.println("  FINAL SIMULATION REPORT");
        System.out.println("  Scenario : " + metrics.getScenarioName());
        System.out.println("  Duration : " + metrics.size() + " months");
        System.out.println("========================================");
        System.out.printf("  End Date            : %s%n", last.getDate());
        System.out.printf("  Final Equity        : %,15.2f%n", last.getEquity());
        System.out.printf("  Final Total Assets  : %,15.2f%n", last.getTotalAssets());
        System.out.printf("  Final Deposits      : %,15.2f%n", last.getTotalDeposits());
        System.out.printf("  Final Loan Book     : %,15.2f%n", last.getTotalLoans());
        System.out.printf("  Final Reserves      : %,15.2f%n", last.getReserves());
        System.out.println("----------------------------------------");
        System.out.printf("  Total Net Income    : %,15.2f%n", metrics.totalNetIncome());
        System.out.printf("  Total Charge-Offs   : %,15.2f%n", metrics.totalChargeOffs());
        System.out.printf("  Total Defaults      : %15d%n", metrics.totalLoansDefaulted());
        System.out.printf("  Peak Equity         : %,15.2f%n", metrics.peakEquity());
        System.out.printf("  Trough Equity       : %,15.2f%n", metrics.troughEquity());
        System.out.printf("  Avg NIM             : %14.3f%%%n", metrics.averageNetInterestMargin() * 100);
        System.out.println("========================================");
    }

    public void printBalanceSheetCheck(SimulationMetrics metrics) {
        System.out.println("\n--- Balance Sheet Consistency ---");
        for (SimulationStepResult s : metrics.getSteps()) {
            double computed = s.getReserves() + s.getTotalLoans();
            double diff = Math.abs(computed - s.getTotalAssets());
            if (diff > 1.0) {
                System.out.printf("WARN Step %d: assets mismatch by %.2f%n", s.getStep(), diff);
            }
        }
        System.out.println("Balance sheet check complete.");
    }
}
