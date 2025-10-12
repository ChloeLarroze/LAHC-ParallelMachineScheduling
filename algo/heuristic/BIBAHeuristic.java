package algo.heuristic;


import domain.*;
import java.util.*;
import solution.*;

/**
 * BIBA 
 *
 * Heuristique gloutonne qui construit une solution en insérant itérativement
 * chaque job à la fin de la machine qui minimise le makespan.
 * 
 * PP :
 * 1. Partir d'une solution vide
 * 2. Pour chaque job non assigné :
 *    a. Évaluer l'insertion à la fin de chaque machine
 *    b. Choisir l'insertion qui donne le meilleur makespan
 * 3. Répéter jusqu'à ce que tous les jobs soient assignés
 */


public class BIBAHeuristic implements Heuristic {
    
    @Override
    public Solution buildInitialSolution(Instance instance) 
    {
        Solution solution = new Solution(instance);
        
        //ensemble des jobs restants à assigner
        Set<Job> remainingJobs = new HashSet<>();
        for (int i = 0; i < instance.getNumberOfJobs(); i++) 
        {
            remainingJobs.add(instance.getJob(i));
        }
        
        //on assign itérativement chaque job
        while (!remainingJobs.isEmpty()) 
        {
            InsertionMove bestMove = findBestInsertion(remainingJobs, solution);
            
            if (bestMove == null) //TODO : faire avec une exception custom ?
            {
                throw new RuntimeException("Not valid insertion");
            }
            
            // meilleure insertion
            Schedule schedule = solution.getSchedule(bestMove.machine);
            schedule.addJob(bestMove.job);
            
            // on retire le job assigné des jobs restants à assigner
            remainingJobs.remove(bestMove.job);
        }
        
        // final makespan 
        solution.calculateMakespan();
        
        return solution;
    }

    
    //helper function to find the best insertion move for the current solution (called in the main loop)
    private InsertionMove findBestInsertion(Set<Job> remainingJobs, Solution solution) {
        InsertionMove bestMove = null;
        int bestMakespan = Integer.MAX_VALUE;

        //parse each remaining job (like the none assigned ones)
        for (Job job : remainingJobs) {
            //each machine 
            for (int m = 0; m < solution.getNumberOfMachines(); m++) 
            {
                Machine machine = solution.getInstance().getMachine(m);
                
                //we evaluate the insertion at the end of the machine bc 
                int makespan = evaluateInsertion(job, machine, solution);
                
                //meilleure insertion
                if (makespan < bestMakespan) 
                {
                    bestMakespan = makespan;
                    bestMove = new InsertionMove(job, machine, makespan);
                }
            }
        }
        //meilleur mouvement d'insertion
        return bestMove;
    }
    
   //same : helper function to evaluate the insertion of a job at the end of a machine 
    private int evaluateInsertion(Job job, Machine machine, Solution solution) {
        // copie pour tester l'insertion //TODO en faire une explication dans le rapport (trouver un truc sur l'effet de bord ?) //TODO
        Solution testSolution = solution.copy();
        Schedule testSchedule = testSolution.getSchedule(machine);
        
        //on le fout à la fin 
        testSchedule.addJob(job);
        
        //calcul new makespan
        testSolution.calculateMakespan();
        return testSolution.getMakespan();
    }
}