package com.boma.banksim.report;

import com.boma.banksim.simulation.SimulationStepResult;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Exports simulation results to a CSV file for further analysis.
 */
public class CsvExporter {

    /**
     * Writes all step results to a CSV file.
     *
     * @param metrics  simulation results to export
     * @param filePath path to the output .csv file
     */
    public void export(SimulationMetrics metrics, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(SimulationStepResult.csvHeader());
            writer.newLine();
            for (SimulationStepResult step : metrics.getSteps()) {
                writer.write(step.toCsvRow());
                writer.newLine();
            }
        }
        System.out.println("Exported " + metrics.size() + " rows to " + filePath);
    }

    /** Writes to a default filename based on the scenario name. */
    public void exportDefault(SimulationMetrics metrics) throws IOException {
        String safe = metrics.getScenarioName().toLowerCase().replaceAll("[^a-z0-9]", "_");
        export(metrics, safe + "_results.csv");
    }
}
