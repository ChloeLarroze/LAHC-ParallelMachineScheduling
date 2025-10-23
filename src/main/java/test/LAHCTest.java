package test;

import  domain.*;
import  solution.*;
import  algo.heuristic.*;
import  algo.metaheuristic.*;

public class LAHCTest {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                      TEST - LAHC                           ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");


        // Créer une instance de test
        Instance instance = createTestInstance();
        
        //LAHC avec BIBA
        BIBAHeuristic biba = new BIBAHeuristic();
        LAHCMetaheuristic lahc = new LAHCMetaheuristic(biba);

        // itérations pour test rapide
        lahc.setMaxIterations(100); //TODO  à ajouter en setter dans LAHCMetaheuristic
        System.out.println("Start ....\n");
        
        //solving part
        Solution solution = lahc.solve(instance);
        
        //résultat
        System.out.println("\n" + "=".repeat(50));
        System.out.println("SOLUTION FINALE:");
        System.out.println("=".repeat(50));
        System.out.println("  Makespan: " + solution.getMakespan());
        
        for (int m = 0; m < 2; m++) {
            Schedule s = solution.getSchedule(m);
            System.out.print("  M" + m + ": [");
            for (Job j : s.getJobSequence()) {
                System.out.print("J" + j.getId() + " ");
            }
            System.out.println("] (C=" + s.getCompletionTime() + ")");
        }
        
        //vérifs
        System.out.println("\n" + "=".repeat(50));
        System.out.println("VÉRIFICATIONS:");
        System.out.println("=".repeat(50));
        
        //jobs assignés ?
        int totalJobs = solution.getTotalJobCount();
        System.out.println("Jobs assignés: " + totalJobs + "/4");
        assert totalJobs == 4 : "ERREUR: Pas tous les jobs";
        
        //solution complète ?
        System.out.println("Solution complète: " + solution.isComplete());
        assert solution.isComplete() : "ERREUR: Solution incomplète!";
        
        //makespan 
        int makespan = solution.getMakespan();
        System.out.println("Makespan calculé: " + makespan);
        assert makespan > 0 : "ERREUR: Makespan invalide!";
        
        //meilleure solution
        Solution best = lahc.getBestSolution();
        System.out.println("Meilleure solution accessible: " + (best != null));
        assert best != null : "ERREUR: Pas de meilleure solution!";
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("TOUS LES TESTS PASSÉS");
        System.out.println("=".repeat(50));
    }

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
    
    
}
