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

        // 4 jobs, 2 machines
        Instance instance = createInstance();
        
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
    
    private static Instance createInstance() {
        //4 jobs, 2 machines, release dates = 0
        int[] releaseDates = {0, 0, 0, 0};
        Instance instance = new Instance(4, 2, releaseDates);
        
        //processing times
        int[][] p = {
            {5, 6},  // J0
            {3, 4},  // J1
            {7, 5},  // J2
            {4, 3}  // J3
        };
        
        for (int j = 0; j < 4; j++) {
            for (int m = 0; m < 2; m++) {
                instance.setProcessingTime(j, m, p[j][m]);
            }
        }
        
        //setup times = 1 partout
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
