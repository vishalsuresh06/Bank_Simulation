package com.boma.banksim.service;

import com.boma.banksim.account.Account;
import com.boma.banksim.bank.Bank;
import com.boma.banksim.customer.Customer;
import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.risk.LiquidityStressModel;
import com.boma.banksim.util.RandomProvider;

import java.time.LocalDate;
import java.util.List;

/**
 * Simulates customer financial behaviour each month:
 *   1. Income deposits   – customers receive salary / business revenue
 *   2. Spending          – customers withdraw funds for living expenses
 *   3. Stress withdrawals – panic withdrawals during economic stress
 */
public class CustomerBehaviorService {

    private final PaymentProcessor paymentProcessor;
    private final LiquidityStressModel stressModel;
    private final RandomProvider random;

    public CustomerBehaviorService(RandomProvider random) {
        this.paymentProcessor = new PaymentProcessor();
        this.stressModel = new LiquidityStressModel();
        this.random = random;
    }

    /**
     * Deposits the customer's monthly income into their primary checking account.
     * Adds small random variation (±10%) to mimic real income variability.
     */
    public void processIncomeDeposit(Bank bank, Customer customer, LocalDate date) {
        Account primary = getPrimaryAccount(bank, customer);
        if (primary == null) return;

        double monthlyIncome = customer.getProfile().getIncome() / 12.0;
        double variation = 1.0 + (random.nextDouble(-0.10, 0.10));
        double deposit = Math.max(0, monthlyIncome * variation);
        if (deposit > 0) {
            paymentProcessor.deposit(bank, primary, deposit, date);
        }
    }

    /**
     * Withdraws spending from the customer's primary account.
     * spendingRate is the fraction of monthly income spent each month.
     * If account balance is insufficient, withdraws whatever is available (floored at $0).
     */
    public void processSpendingWithdrawal(Bank bank, Customer customer, LocalDate date) {
        Account primary = getPrimaryAccount(bank, customer);
        if (primary == null) return;

        double monthlyIncome = customer.getProfile().getIncome() / 12.0;
        double spending = monthlyIncome * customer.getProfile().getSpendingRate();
        double actualSpending = Math.min(spending, primary.getBalance() * 0.95); // keep 5% buffer
        if (actualSpending > 1.0) {
            paymentProcessor.withdraw(bank, primary, actualSpending, date);
        }
    }

    /**
     * Panic withdrawal triggered by low depositor confidence.
     * Only fires if this customer is sensitive to economic stress.
     */
    public void processStressWithdrawal(Bank bank, Customer customer,
                                         EconomicEnvironment economy, LocalDate date) {
        if (!economy.isStressed()) return;
        Account primary = getPrimaryAccount(bank, customer);
        if (primary == null || primary.getBalance() < 100) return;

        double stressRate = stressModel.stressedWithdrawalRate(economy, customer.getProfile());
        if (stressRate < 0.01) return; // too small to bother

        // Only a random fraction of stress-sensitive customers actually panic
        double individualPanic = stressRate * customer.getProfile().getWithdrawalSensitivity();
        if (!random.nextChance(individualPanic)) return;

        double withdrawal = primary.getBalance() * stressRate;
        withdrawal = Math.min(withdrawal, primary.getBalance() * 0.90); // leave 10%
        if (withdrawal > 1.0) {
            paymentProcessor.withdraw(bank, primary, withdrawal, date);
        }
    }

    /** Processes all three behaviours for every customer. */
    public void processAllCustomers(Bank bank, EconomicEnvironment economy, LocalDate date) {
        for (Customer customer : bank.getAllCustomers()) {
            processIncomeDeposit(bank, customer, date);
            processSpendingWithdrawal(bank, customer, date);
            processStressWithdrawal(bank, customer, economy, date);
        }
    }

    /** Returns the customer's first account (assumed to be their primary). */
    private Account getPrimaryAccount(Bank bank, Customer customer) {
        List<String> ids = customer.getAccountIds();
        if (ids.isEmpty()) return null;
        return bank.getAccount(ids.get(0));
    }
}
