package test;

import algo.heuristic.BIBAHeuristic;
import algo.metaheuristic.LAHCMetaheuristic;
import domain.Instance;
import solution.Solution;
import utils.InstanceReader;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class BenchmarkTest {
    private static final String CSV_FILE = "benchmark_results.csv";
    
    public static void main(String[] args) throws Exception {
        // Initialize CSV with headers
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
            writer.println("Instance,Iterations,ExecutionTime,LastImprovementIter,InitialMakespan,FinalMakespan,BestMakespan,ImprovementPercent");
        }
        
        for (int i = 0; i < 100; i++) {
            int randomJobNumber = 1 + (int)(Math.random() * 10); // between 1 and 10
            int randomMachineNumber = 1 + (int)(Math.random() * 5); // between 1 and 5
            Instance instance = InstanceReader.createRandomInstance(randomJobNumber, randomMachineNumber, 10, 5, 0.5);
            solveLAHC(instance, i);
        }
    }

    private static void solveLAHC(Instance instance, int runNumber) {
    BIBAHeuristic biba = new BIBAHeuristic();
    LAHCMetaheuristic lahc = new LAHCMetaheuristic(biba);
    
    long startTime = System.currentTimeMillis();
    Solution solution = lahc.solve(instance);
    long endTime = System.currentTimeMillis();
    long executionTime = endTime - startTime;

    int iterationCount = lahc.getIterationCount();
    //double improvementPercent = 100.0 * (lahc.getInitialMakespan() - lahc.getBestSolution().getMakespan()) / lahc.getInitialMakespan();
    double improvementPercent = 100.0 * (lahc.getInitialMakespan() - lahc.getBestMakespan()) / lahc.getInitialMakespan();

    
    // FIX: Use dot as decimal separator and proper formatting
    try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE, true))) {
        writer.printf("%d,%d,%.3f,%d,%d,%d,%d,%.2f%n",
        runNumber,
        iterationCount,
        executionTime / 1000.0,
        lahc.getLastImprovementIteration(),
        lahc.getInitialMakespan(),
        lahc.getBestSolution().getMakespan(),
        lahc.getBestMakespan(),
        improvementPercent);
    } catch (IOException e) {
        System.err.println("Error writing to CSV file: " + e.getMessage());
        }
    }
}