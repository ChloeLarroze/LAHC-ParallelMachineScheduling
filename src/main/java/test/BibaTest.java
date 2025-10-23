package test;

import algo.heuristic.*;
import domain.*;
import solution.*;


public class BibaTest {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    TEST - BIBA                             ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
        
        //instance: 4 jobs, 2 machines
        Instance instance = createInstance();
        BIBAHeuristic biba = new BIBAHeuristic();
        Solution solution = biba.buildInitialSolution(instance);
        
        //résultat
        System.out.println("SOLUTION:");
        System.out.println("  Makespan: " + solution.getMakespan());
        for (int m = 0; m < 2; m++) 
        {
            Schedule s = solution.getSchedule(m);
            System.out.print("  M" + m + ": [");
            for (Job j : s.getJobSequence()) {
                System.out.print("J" + j.getId() + " ");
            }
            System.out.println("] (C=" + s.getCompletionTime() + ")");
        }
        
        //vérif
        System.out.println("\nVÉRIFICATIONS:");
        
        //tous les jobs assignés ?
        int totalJobs = solution.getTotalJobCount();
        System.out.println("  Jobs assignés: " + totalJobs + "/4");
        assert totalJobs == 4 : "ERREUR: Pas tous les jobs!";
        
        // solution complète ?
        System.out.println("  Solution complète: " + solution.isComplete());
        assert solution.isComplete() : "ERREUR: Solution incomplète!";
        
        // Makespan valide ?
        int makespan = solution.getMakespan();
        System.out.println("  Makespan calculé: " + makespan);
        assert makespan > 0 : "ERREUR: Makespan invalide!";
        
        System.out.println("\n✓ TOUS LES TESTS PASSÉS ✓");
    }
    
    private static Instance createInstance() {
        // Instance simple: 4 jobs, 2 machines, release dates = 0
        int[] releaseDates = {0, 0, 0, 0};
        Instance instance = new Instance(4, 2, releaseDates);
        
        // Processing times
        int[][] p = {
            {5, 6},  // J0
            {3, 4},  // J1
            {7, 5},  // J2
            {4, 3}   // J3
        };
        
        for (int j = 0; j < 4; j++) {
            for (int m = 0; m < 2; m++) {
                instance.setProcessingTime(j, m, p[j][m]);
            }
        }
        
        // Setup times = 1 partout
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int m = 0; m < 2; m++) {
                    instance.setSetupTime(i, j, m, 1);
                }
            }
        }
        
        System.out.println("Instance créée: 4 jobs, 2 machines");
        System.out.println("Release dates: tous à 0");
        System.out.println("Processing times et setup times définis\n");
        
        return instance;
    }
}