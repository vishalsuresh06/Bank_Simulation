package com.boma.banksim.app;

import com.boma.banksim.bank.Bank;
import com.boma.banksim.economy.EconomicEnvironment;
import com.boma.banksim.report.BankReport;
import com.boma.banksim.report.CsvExporter;
import com.boma.banksim.report.ReportGenerator;
import com.boma.banksim.report.SimulationMetrics;
import com.boma.banksim.simulation.Scenario;
import com.boma.banksim.simulation.SimulationClock;
import com.boma.banksim.simulation.SimulationEngine;
import com.boma.banksim.util.RandomProvider;

import java.io.IOException;

/**
 * Wires all components together and runs a single simulation scenario.
 */
public class SimulationRunner {

    private final ScenarioLoader loader = new ScenarioLoader();
    private final ReportGenerator reporter = new ReportGenerator();
    private final BankReport bankReport = new BankReport();
    private final CsvExporter csvExporter = new CsvExporter();

    public SimulationMetrics run(Scenario scenario) {
        System.out.println("=== Boma Bank Simulation ===");
        System.out.println("Scenario : " + scenario.getName());
        System.out.println("Duration : " + scenario.getDurationMonths() + " months");
        System.out.println("Customers: " + scenario.getNumCustomers());
        System.out.println();

        // Build bank and set up infrastructure
        Bank bank = loader.buildBank(scenario);
        EconomicEnvironment economy = scenario.getEconomy();
        SimulationClock clock = new SimulationClock(scenario.getStartDate());
        SimulationEngine engine = new SimulationEngine(new RandomProvider(scenario.getRandomSeed()));

        // Print opening balance sheet
        System.out.println("\n--- Opening Balance Sheet ---");
        bankReport.print(bank, scenario.getStartDate());

        // Run the simulation
        System.out.println("\n--- Monthly Steps ---");
        SimulationMetrics metrics = engine.run(bank, economy, clock, scenario);

        // Print closing balance sheet
        System.out.println("\n--- Closing Balance Sheet ---");
        bankReport.print(bank, clock.getCurrentDate());

        // Final summary report
        reporter.printFinalReport(metrics);
        reporter.printBalanceSheetCheck(metrics);

        // Export CSV
        try {
            csvExporter.exportDefault(metrics);
        } catch (IOException e) {
            System.err.println("CSV export failed: " + e.getMessage());
        }

        return metrics;
    }
}
