package test;

import algo.heuristic.BIBAHeuristic;
import algo.metaheuristic.LAHCMetaheuristic;
import domain.Instance;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import solution.Solution;
import utils.InstanceReader;

public class BenchmarkTest {
    private static final String CSV_FILE = "resources/out/benchmark_results.csv";
    private static final int NUM_RUNS = 100;
    
    public static void main(String[] args) throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║              BENCHMARK - LAHC Performance                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
        
        // Initialize CSV with headers
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
            writer.println("run_id,jobs,machines,initial_makespan,final_makespan,improvement_pct,iterations,last_improvement_iter,exec_time_ms,timestamp");
        }
        
        System.out.printf("Running %d instances...\n\n", NUM_RUNS);
        
        long totalStartTime = System.currentTimeMillis();
        
        for (int i = 0; i < NUM_RUNS; i++) {
            int randomJobNumber = 5 + (int)(Math.random() * 16); // 5-20
            int randomMachineNumber = 2 + (int)(Math.random() * 4); // 2-5
            Instance instance = InstanceReader.createRandomInstance(
                randomJobNumber, 
                randomMachineNumber, 
                10, // max processing time
                5,  // max setup time
                0.5 // release date factor
            );
            
            System.out.printf("Run %3d/%d | Jobs: %2d | Machines: %d | ", 
                i+1, NUM_RUNS, randomJobNumber, randomMachineNumber);
            
            solveLAHC(instance, i+1);
            
            if ((i+1) % 10 == 0) {
                System.out.println("\n" + "─".repeat(60));
            }
        }
        
        long totalEndTime = System.currentTimeMillis();
        double totalTime = (totalEndTime - totalStartTime) / 1000.0;
        
        System.out.println("\n" + "═".repeat(60));
        System.out.printf("Benchmark completed in %.2f seconds\n", totalTime);
        System.out.println("═".repeat(60));
    }

    private static void solveLAHC(Instance instance, int runNumber) {
        BIBAHeuristic biba = new BIBAHeuristic();
        LAHCMetaheuristic lahc = new LAHCMetaheuristic(biba);
        
        long startTime = System.currentTimeMillis();
        Solution solution = lahc.solve(instance);
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        int initialMakespan = lahc.getInitialMakespan();
        int finalMakespan = lahc.getBestMakespan();
        double improvementPercent = 100.0 * (initialMakespan - finalMakespan) / (double) initialMakespan;
        
        int iterationCount = lahc.getIterationCount();
        int lastImprovementIter = lahc.getLastImprovementIteration();
        
        //progress
        System.out.printf("Cmax: %3d → %3d (%.1f%%) | %d iters\n",
            initialMakespan, finalMakespan, improvementPercent, iterationCount);
        
        // 2 CSV w/ timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE, true))) {
            writer.printf("%d,%d,%d,%d,%d,%.2f,%d,%d,%d,%s%n",
                runNumber,
                instance.getNumberOfJobs(),
                instance.getNumberOfMachines(),
                initialMakespan,
                finalMakespan,
                improvementPercent,
                iterationCount,
                lastImprovementIter,
                executionTime,
                timestamp
            );
        } catch (IOException e) {
            System.err.println("Error CSV: " + e.getMessage());
        }
    }
}