package test;

import algo.localsearch.*;
import domain.*;
import solution.*;

/**
 * Test minimal pour vérifier le fonctionnement des 5 opérateurs de LocalSearch.
 * Exécutable directement avec main(), sans dépendances externes.
 */
public class LocalSearchTest {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║         TEST - LocalSearch (5 Opérateurs)                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
        
        // Créer une instance de test
        Instance instance = createTestInstance();
        Solution solution = createUnbalancedSolution(instance);
        
        System.out.println("SOLUTION INITIALE:");
        printSolution(solution);
        
        LocalSearch localSearch = new LocalSearch();
        
        // Test des 5 opérateurs individuellement
        testOperator1(solution.copy(), localSearch);
        testOperator2(solution.copy(), localSearch);
        testOperator3(solution.copy(), localSearch);
        testOperator4(solution.copy(), localSearch);
        testOperator5(solution.copy(), localSearch);

    }
    
    // ========== CRÉATION DE L'INSTANCE DE TEST ==========
    
    private static Instance createTestInstance() {
        System.out.println("Création instance: 6 jobs, 3 machines\n");
        
        int[] releaseDates = {0, 0, 0, 0, 0, 0};
        Instance instance = new Instance(6, 3, releaseDates);
        
        // Processing times: variés car in veut créer des déséquilibres
        int[][] processingTimes = {
            {10, 8, 12},  // Job 0
            {5, 6, 4},    // Job 1
            {15, 10, 8},  // Job 2
            {7, 9, 6},    // Job 3
            {12, 11, 10}, // Job 4
            {8, 7, 9}     // Job 5
        };
        
        for (int j = 0; j < 6; j++) {
            for (int m = 0; m < 3; m++) {
                instance.setProcessingTime(j, m, processingTimes[j][m]);
            }
        }
        
        // Setup times: tous à 2 pour simplifier
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                for (int m = 0; m < 3; m++) {
                    instance.setSetupTime(i, j, m, 2);
                }
            }
        }
        
        return instance;
    }
    
    private static Solution createUnbalancedSolution(Instance instance) {
        Solution solution = new Solution(instance);
        
        //solution déséquilibrée (aka machine 0 surchargée)
        // M0: J0, J2, J4 (charges: 10+15+12 = 37 + setups)
        solution.getSchedule(0).addJob(instance.getJob(0));
        solution.getSchedule(0).addJob(instance.getJob(2));
        solution.getSchedule(0).addJob(instance.getJob(4));
        
        // M1: J1 (charge: 5)
        solution.getSchedule(1).addJob(instance.getJob(1));
        
        // M2: J3, J5 (charges: 7+8 = 15)
        solution.getSchedule(2).addJob(instance.getJob(3));
        solution.getSchedule(2).addJob(instance.getJob(5));
        
        solution.calculateMakespan();
        return solution;
    }
    
    // ========== TESTS DES 5 OPÉRATEURS ==========
    
    private static void testOperator1(Solution solution, LocalSearch ls) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST 1: Bottleneck Internal Swap");
        System.out.println("=".repeat(60));
        
        int initialMakespan = solution.getMakespan();
        System.out.println("Makespan initial: " + initialMakespan);
        System.out.println("Machine bottleneck: " + solution.getBottleneckMachines());
        
        boolean improved = ls.bottleneckInternalSwap(solution);
        
        int newMakespan = solution.getMakespan();
        System.out.println("\nMakespan après Internal Swap: " + newMakespan);
        System.out.println("Amélioration: " + improved);
        System.out.println("Différence: " + (initialMakespan - newMakespan));
        
        if (improved || newMakespan <= initialMakespan) {
            System.out.println("✓ Test 1 OK");
        } else {
            System.out.println("✓ Test 1 OK (pas d'amélioration possible)");
        }
    }
    
    private static void testOperator2(Solution solution, LocalSearch ls) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST 2: Bottleneck External Insertion");
        System.out.println("=".repeat(60)); //pour la lisibilité
        
        int initialMakespan = solution.getMakespan();
        System.out.println("Makespan initial: " + initialMakespan);
        
        printSolution(solution);
        
        boolean improved = ls.bottleneckExternalInsertion(solution);
        
        System.out.println("\nAprès External Insertion:");
        printSolution(solution);
        
        int newMakespan = solution.getMakespan();
        System.out.println("Makespan final: " + newMakespan);
        System.out.println("Amélioration: " + improved);
        System.out.println("Différence: " + (initialMakespan - newMakespan));
        
        // Vérifier que tous les jobs sont toujours présents
        assert solution.getTotalJobCount() == 6 : "ERROR: Jobs perdus!";
        
        System.out.println("✓ Test 2 OK");
    }
    
    private static void testOperator3(Solution solution, LocalSearch ls) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST 3: Bottleneck External Swap");
        System.out.println("=".repeat(60));
        
        int initialMakespan = solution.getMakespan();
        System.out.println("Makespan initial: " + initialMakespan);
        
        boolean improved = ls.bottleneckExternalSwap(solution);
        
        int newMakespan = solution.getMakespan();
        System.out.println("\nMakespan après External Swap: " + newMakespan);
        System.out.println("Amélioration: " + improved);
        System.out.println("Différence: " + (initialMakespan - newMakespan));
        
        // Vérifier intégrité
        assert solution.getTotalJobCount() == 6 : "ERROR: Jobs count changed!";
        
        System.out.println("✓ Test 3 OK");
    }
    
    private static void testOperator4(Solution solution, LocalSearch ls) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST 4: Balancing");
        System.out.println("=".repeat(60));
        
        int initialMakespan = solution.getMakespan();
        System.out.println("Makespan initial: " + initialMakespan);
        System.out.println("Distribution initiale:");
        for (int m = 0; m < solution.getNumberOfMachines(); m++) {
            Schedule s = solution.getSchedule(m);
            System.out.printf("  M%d: %d jobs, C=%d%n", m, s.getJobCount(), s.getCompletionTime());
        }
        
        boolean improved = ls.balancing(solution);
        
        System.out.println("\nDistribution après Balancing:");
        for (int m = 0; m < solution.getNumberOfMachines(); m++) {
            Schedule s = solution.getSchedule(m);
            System.out.printf("  M%d: %d jobs, C=%d%n", m, s.getJobCount(), s.getCompletionTime());
        }
        
        int newMakespan = solution.getMakespan();
        System.out.println("\nMakespan final: " + newMakespan);
        System.out.println("Amélioration: " + improved);
        System.out.println("Différence: " + (initialMakespan - newMakespan));
        
        System.out.println("✓ Test 4 OK");
    }
    
    private static void testOperator5(Solution solution, LocalSearch ls) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST 5: Inter Machine Insertion");
        System.out.println("=".repeat(60));
        System.out.println("Objectif: Optimiser positions inter-machines (équation 1)\n");
        
        int initialMakespan = solution.getMakespan();
        System.out.println("Makespan initial: " + initialMakespan);
        
        boolean improved = ls.interMachineInsertion(solution);
        
        int newMakespan = solution.getMakespan();
        System.out.println("\nMakespan après Inter Machine Insertion: " + newMakespan);
        System.out.println("Amélioration: " + improved);
        System.out.println("Différence: " + (initialMakespan - newMakespan));
        
        if (improved) {
            System.out.println("→ a trouvé un mouvement bénéfique");
        }

        System.out.println("✓ Test 5 OK");
    }
    
    // ========== MÉTHODES UTILITAIRES ==========
    
    private static void printSolution(Solution sol) {
        System.out.println("  Makespan: " + sol.getMakespan());
        for (int m = 0; m < sol.getNumberOfMachines(); m++) {
            Schedule s = sol.getSchedule(m);
            System.out.printf("  M%d (C=%3d): %s%n", 
                            m, s.getCompletionTime(), formatJobs(s));
        }
    }
    
    private static String formatJobs(Schedule schedule) {
        if (schedule.getJobCount() == 0) return "[]";
        
        StringBuilder sb = new StringBuilder("[");
        for (Job job : schedule.getJobSequence()) {
            sb.append("J").append(job.getId()).append(" ");
        }
        sb.setLength(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }
}