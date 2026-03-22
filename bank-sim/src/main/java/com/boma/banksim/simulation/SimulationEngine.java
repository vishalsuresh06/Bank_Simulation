package com.boma.banksim.simulation;

import com.boma.banksim.bank.Bank;
import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.economy.MarketShock;
import com.boma.banksim.loan.Loan;
import com.boma.banksim.loan.LoanStatus;
import com.boma.banksim.report.SimulationMetrics;
import com.boma.banksim.service.CustomerBehaviorService;
import com.boma.banksim.service.DefaultService;
import com.boma.banksim.service.InterestAccrualService;
import com.boma.banksim.service.LoanService;
import com.boma.banksim.util.RandomProvider;

import java.time.LocalDate;
import java.util.List;

/**
 * Orchestrates the monthly simulation loop.
 *
 * Each step in order:
 *   1.  Apply any scheduled market shocks
 *   2.  Customer income deposits
 *   3.  Accrue deposit interest  (bank pays depositors)
 *   4.  Collect loan payments    (customers pay the bank)
 *   5.  Evaluate delinquency     (missed payments → status update)
 *   6.  Default evaluation       (delinquent loans → charge-offs)
 *   7.  Customer spending withdrawals
 *   8.  Stress withdrawals       (panic behaviour during downturns)
 *   9.  Record step result snapshot
 *  10.  Reset period income counters on balance sheet
 */
public class SimulationEngine {

    private final CustomerBehaviorService behaviorService;
    private final InterestAccrualService interestService;
    private final LoanService loanService;
    private final DefaultService defaultService;

    public SimulationEngine(RandomProvider random) {
        this.behaviorService = new CustomerBehaviorService(random);
        this.interestService = new InterestAccrualService();
        this.loanService = new LoanService();
        this.defaultService = new DefaultService(random);
    }

    /**
     * Runs the full simulation for the given scenario.
     *
     * @param bank     the bank to simulate (pre-populated with customers/accounts)
     * @param economy  the starting economic environment
     * @param clock    simulation clock (set to scenario start date)
     * @param scenario scenario configuration including shocks
     * @return collected metrics for reporting
     */
    public SimulationMetrics run(
            Bank bank,
            EconomicEnvironment economy,
            SimulationClock clock,
            Scenario scenario
    ) {
        SimulationMetrics metrics = new SimulationMetrics(scenario.getName());
        EventQueue eventQueue = new EventQueue();

        // Schedule all market shocks
        for (MarketShock shock : scenario.getShocks()) {
            eventQueue.schedule(shock.getScheduledDate(), shock.getDescription(),
                    () -> {
                        shock.apply(economy);
                        System.out.println("  [SHOCK] " + shock.getDescription() +
                                " → " + economy.getState());
                    });
        }

        System.out.println("Starting simulation: " + scenario);
        System.out.println();

        for (int step = 1; step <= scenario.getDurationMonths(); step++) {
            LocalDate date = clock.advanceOneMonth();
            bank.getBalanceSheet().resetPeriodCounters();

            // 1. Apply scheduled shocks
            eventQueue.processUpTo(date);

            // 2–3. Customer income and deposit interest
            for (var customer : bank.getAllCustomers()) {
                behaviorService.processIncomeDeposit(bank, customer, date);
            }
            interestService.accrueDepositInterest(bank, date);

            // 4. Collect loan payments from customers who can afford them
            int newLoanPayments = 0;
            for (Loan loan : bank.getAllLoans()) {
                if (!loan.isActive()) continue;
                var customer = bank.getCustomer(loan.getCustomerId());
                if (customer == null) continue;

                var accounts = customer.getAccountIds();
                for (String accountId : accounts) {
                    var account = bank.getAccount(accountId);
                    if (account != null && account.getBalance() >= loan.getMonthlyPayment()) {
                        account.withdraw(loan.getMonthlyPayment());
                        bank.getBalanceSheet().decreaseDeposits(loan.getMonthlyPayment());
                        loanService.processPayment(bank, loan, loan.getMonthlyPayment(), date);
                        newLoanPayments++;
                        break;
                    }
                }
            }

            // 5. Delinquency evaluation
            loanService.evaluateDelinquency(bank);

            // 6. Default and charge-off
            List<Loan> chargedOff = defaultService.evaluateAndChargeOff(bank, economy, date);

            // 7–8. Spending and stress withdrawals
            for (var customer : bank.getAllCustomers()) {
                behaviorService.processSpendingWithdrawal(bank, customer, date);
                behaviorService.processStressWithdrawal(bank, customer, economy, date);
            }

            // 9. Capture snapshot
            int activeLoans = (int) bank.getAllLoans().stream().filter(Loan::isActive).count();
            int numDefaulted = (int) bank.getAllLoans().stream()
                    .filter(l -> l.getStatus() == LoanStatus.DEFAULTED).count();

            SimulationStepResult result = new SimulationStepResult(
                    date, step,
                    bank.getBalanceSheet().getReserves(),
                    bank.getBalanceSheet().getTotalLoans(),
                    bank.getBalanceSheet().getTotalDeposits(),
                    bank.getBalanceSheet().getEquity(),
                    bank.getBalanceSheet().getTotalAssets(),
                    bank.getBalanceSheet().getGrossInterestIncome(),
                    bank.getBalanceSheet().getDepositInterestExpense(),
                    bank.getBalanceSheet().getChargeOffLosses(),
                    bank.getBalanceSheet().getNetIncome(),
                    activeLoans,
                    numDefaulted,
                    chargedOff.size(),
                    newLoanPayments,
                    economy.getPolicyRate(),
                    economy.getUnemploymentRate(),
                    economy.getDepositorConfidence()
            );

            metrics.record(result);
            printStep(result);
        }

        return metrics;
    }

    private void printStep(SimulationStepResult r) {
        System.out.printf(
                "Step %3d [%s] | Assets=%,12.0f | Equity=%,10.0f | NII=%,7.0f | Defaults=%d | ChargeOffs=%d%n",
                r.getStep(), r.getDate(),
                r.getTotalAssets(), r.getEquity(),
                r.getGrossInterestIncome() - r.getDepositInterestExpense(),
                r.getNumDefaulted(), r.getNumChargedOff()
        );
    }
}
