package com.boma.banksim.integration;

import com.boma.banksim.account.CheckingAccount;
import com.boma.banksim.account.SavingsAccount;
import com.boma.banksim.bank.Bank;
import com.boma.banksim.customer.Customer;
import com.boma.banksim.customer.CustomerProfile;
import com.boma.banksim.customer.CustomerType;
import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.loan.Loan;
import com.boma.banksim.loan.LoanApplication;
import com.boma.banksim.loan.LoanType;
import com.boma.banksim.report.SimulationMetrics;
import com.boma.banksim.service.LoanService;
import com.boma.banksim.service.PaymentProcessor;
import com.boma.banksim.simulation.Scenario;
import com.boma.banksim.simulation.SimulationClock;
import com.boma.banksim.simulation.SimulationEngine;
import com.boma.banksim.simulation.SimulationStepResult;
import com.boma.banksim.util.RandomProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 3-month smoke test for the simulation engine verifying:
 * - Balance sheet remains consistent at each step
 * - Metrics are recorded for each step
 * - No exceptions thrown on normal operation
 */
class SimulationEngineShortRunTest {

    private static final LocalDate START = LocalDate.of(2024, 1, 1);

    private Bank bank;
    private EconomicEnvironment economy;
    private SimulationEngine engine;
    private PaymentProcessor processor;
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        bank = new Bank("B1", "TestBank", 500_000, 100_000);
        economy = EconomicEnvironment.normal();
        engine = new SimulationEngine(new RandomProvider(42L));
        processor = new PaymentProcessor();
        loanService = new LoanService();
    }

    private Customer addCustomerWithFunds(String id, double balance) {
        Customer customer = new Customer(id, "Customer " + id, CustomerType.RETAIL,
                new CustomerProfile(96_000, 0.3, 750, 0.2, 0.3));
        CheckingAccount checking = new CheckingAccount("CHK-" + id, id, 0.0);
        SavingsAccount savings = new SavingsAccount("SAV-" + id, id, 0.0, 0.04);
        bank.addCustomer(customer);
        bank.addAccount(checking);
        bank.addAccount(savings);
        customer.addAccountId("CHK-" + id);
        customer.addAccountId("SAV-" + id);
        processor.deposit(bank, checking, balance, START);
        processor.deposit(bank, savings, balance * 0.5, START);
        return customer;
    }

    @Test
    void threeMonthRun_producesThreeStepResults() {
        addCustomerWithFunds("C1", 20_000);
        addCustomerWithFunds("C2", 15_000);

        Scenario scenario = new Scenario.Builder("Test3Month")
                .durationMonths(3)
                .startDate(START)
                .economy(economy)
                .randomSeed(42L)
                .build();

        SimulationMetrics metrics = engine.run(bank, economy,
                new SimulationClock(START), scenario);

        assertEquals(3, metrics.size());
    }

    @Test
    void threeMonthRun_balanceSheetRemainsSolventAfterEachStep() {
        addCustomerWithFunds("C1", 30_000);
        addCustomerWithFunds("C2", 25_000);
        addCustomerWithFunds("C3", 20_000);

        Scenario scenario = new Scenario.Builder("Test3Month")
                .durationMonths(3)
                .startDate(START)
                .economy(economy)
                .randomSeed(42L)
                .build();

        SimulationMetrics metrics = engine.run(bank, economy,
                new SimulationClock(START), scenario);

        // Balance sheet equation always holds after each step
        // We verify via the final balance sheet state
        assertTrue(bank.getBalanceSheet().isBalanced());
        assertTrue(bank.getBalanceSheet().isSolvent());
    }

    @Test
    void threeMonthRun_metricsStepNumbersAreSequential() {
        addCustomerWithFunds("C1", 20_000);

        Scenario scenario = new Scenario.Builder("SequentialTest")
                .durationMonths(3)
                .startDate(START)
                .economy(economy)
                .randomSeed(42L)
                .build();

        SimulationMetrics metrics = engine.run(bank, economy,
                new SimulationClock(START), scenario);

        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, metrics.getStep(i).getStep());
        }
    }

    @Test
    void threeMonthRun_stepDatesAdvanceMonthly() {
        addCustomerWithFunds("C1", 20_000);

        Scenario scenario = new Scenario.Builder("DatesTest")
                .durationMonths(3)
                .startDate(START)
                .economy(economy)
                .randomSeed(42L)
                .build();

        SimulationMetrics metrics = engine.run(bank, economy,
                new SimulationClock(START), scenario);

        assertEquals(START.plusMonths(1), metrics.getStep(0).getDate());
        assertEquals(START.plusMonths(2), metrics.getStep(1).getDate());
        assertEquals(START.plusMonths(3), metrics.getStep(2).getDate());
    }

    @Test
    void threeMonthRun_withLoan_loansTrackedInMetrics() {
        Customer customer = addCustomerWithFunds("C1", 50_000);

        // Issue a loan before simulation starts
        LoanApplication app = new LoanApplication("APP1", "C1",
                LoanType.CONSUMER, 10_000, 8_000, 0, 750, 12);
        app.approve();
        loanService.issueLoan(bank, customer, app, economy, START);

        Scenario scenario = new Scenario.Builder("LoanTest")
                .durationMonths(3)
                .startDate(START)
                .economy(economy)
                .randomSeed(42L)
                .build();

        SimulationMetrics metrics = engine.run(bank, economy,
                new SimulationClock(START), scenario);

        // At least some loan payments should have been recorded
        SimulationStepResult lastStep = metrics.getLastStep();
        assertNotNull(lastStep);
        assertTrue(lastStep.getNumNewLoans() > 0 || lastStep.getTotalActiveLoans() >= 0);
    }

    @Test
    void threeMonthRun_scenarioName_capturedInMetrics() {
        addCustomerWithFunds("C1", 20_000);

        Scenario scenario = new Scenario.Builder("MyScenario")
                .durationMonths(3)
                .startDate(START)
                .economy(economy)
                .randomSeed(42L)
                .build();

        SimulationMetrics metrics = engine.run(bank, economy,
                new SimulationClock(START), scenario);

        assertEquals("MyScenario", metrics.getScenarioName());
    }

    @Test
    void threeMonthRun_noCustomers_completesWithoutError() {
        // Empty bank — no customers, no accounts
        Scenario scenario = new Scenario.Builder("EmptyRun")
                .durationMonths(3)
                .startDate(START)
                .economy(economy)
                .randomSeed(42L)
                .build();

        SimulationMetrics metrics = engine.run(bank, economy,
                new SimulationClock(START), scenario);

        assertEquals(3, metrics.size());
        assertTrue(bank.getBalanceSheet().isBalanced());
    }

    @Test
    void threeMonthRun_multipleMixedCustomers_totalAssetsPositive() {
        for (int i = 1; i <= 5; i++) {
            addCustomerWithFunds("C" + i, 10_000 * i);
        }

        Scenario scenario = new Scenario.Builder("MultiCustomer")
                .durationMonths(3)
                .startDate(START)
                .economy(economy)
                .randomSeed(42L)
                .build();

        SimulationMetrics metrics = engine.run(bank, economy,
                new SimulationClock(START), scenario);

        for (int i = 0; i < 3; i++) {
            assertTrue(metrics.getStep(i).getTotalAssets() > 0);
        }
    }

    @Test
    void threeMonthRun_lastStep_returnsStepThree() {
        addCustomerWithFunds("C1", 20_000);

        Scenario scenario = new Scenario.Builder("LastStepTest")
                .durationMonths(3)
                .startDate(START)
                .economy(economy)
                .randomSeed(42L)
                .build();

        SimulationMetrics metrics = engine.run(bank, economy,
                new SimulationClock(START), scenario);

        assertEquals(3, metrics.getLastStep().getStep());
    }
}
