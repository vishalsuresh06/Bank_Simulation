package com.boma.banksim.app;

import com.boma.banksim.simulation.Scenario;

/**
 * Entry point for the Boma Bank Simulation.
 *
 * Run with: mvn compile exec:java -Dexec.mainClass="com.boma.banksim.app.Main"
 * Or simply: mvn package && java -jar target/bank-sim-1.0-SNAPSHOT.jar
 *
 * Edit the scenario here to switch between presets.
 */
public class Main {

    public static void main(String[] args) {
        // Choose a scenario: baseline() or recession()
        Scenario scenario = parseArgs(args);

        SimulationRunner runner = new SimulationRunner();
        runner.run(scenario);
    }

    private static Scenario parseArgs(String[] args) {
        if (args.length > 0) {
            return switch (args[0].toLowerCase()) {
                case "recession" -> Scenario.recession();
                case "baseline"  -> Scenario.baseline();
                default -> {
                    System.out.println("Unknown scenario '" + args[0] + "', defaulting to baseline.");
                    yield Scenario.baseline();
                }
            };
        }
        return Scenario.baseline();
    }
}
