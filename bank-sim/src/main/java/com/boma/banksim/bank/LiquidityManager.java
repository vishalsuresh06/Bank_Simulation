package com.boma.banksim.bank;

/**
 * Monitors reserve adequacy. Banks must hold a minimum fraction of deposits
 * as liquid reserves to meet withdrawal demand.
 *
 * Required reserves = minimumReserveRatio × totalDeposits
 * Excess reserves  = actualReserves − requiredReserves
 */
public class LiquidityManager {

    /** Regulatory minimum reserve ratio (10% = 0.10). */
    private final double minimumReserveRatio;

    public LiquidityManager(double minimumReserveRatio) {
        if (minimumReserveRatio < 0 || minimumReserveRatio > 1) {
            throw new IllegalArgumentException("Reserve ratio must be in [0, 1].");
        }
        this.minimumReserveRatio = minimumReserveRatio;
    }

    public LiquidityManager() {
        this(0.10);
    }

    public double getMinimumReserveRatio() { return minimumReserveRatio; }

    /** Minimum reserves the bank must hold based on its current deposits. */
    public double requiredReserves(BankBalanceSheet sheet) {
        return sheet.getTotalDeposits() * minimumReserveRatio;
    }

    /** Actual reserves minus required reserves (positive = surplus). */
    public double excessReserves(BankBalanceSheet sheet) {
        return sheet.getReserves() - requiredReserves(sheet);
    }

    /** Actual reserves ÷ total deposits. */
    public double reserveRatio(BankBalanceSheet sheet) {
        if (sheet.getTotalDeposits() == 0) return Double.MAX_VALUE;
        return sheet.getReserves() / sheet.getTotalDeposits();
    }

    /** True if actual reserves meet or exceed the required minimum. */
    public boolean isAdequate(BankBalanceSheet sheet) {
        return sheet.getReserves() >= requiredReserves(sheet);
    }

    /** True if the bank is below minimum reserves (liquidity stress). */
    public boolean isUnderReserved(BankBalanceSheet sheet) {
        return !isAdequate(sheet);
    }

    @Override
    public String toString() {
        return "LiquidityManager{minReserveRatio=" + minimumReserveRatio + '}';
    }
}
