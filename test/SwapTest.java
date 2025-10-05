package test;

import domain.*;
import solution.*;
import algo.neighborhood.*;
//import java.util.Random; //useless here since we fixed the seed


public class SwapTest {
    
    public static void main(String[] args) {
        
        //instance
        Instance instance = createSimpleInstance();
        Solution solution = createSimpleSolution(instance);
        
        System.out.println("INIT SOL:");
        printSolution(solution);
        
        //1 Internal Swap
        System.out.println("\n--- TEST 1: INTERNAL SWAP ---");
        testInternalSwap(solution);
        
        //2 External Swap
        System.out.println("\n--- TEST 2: EXTERNAL SWAP ---");
        testExternalSwap(solution);
        
        System.out.println("\n=== ALL GOOD 👍===");
    }
    
    //functs (we'll take the eg from the article with 4 jobs and 2 machines and fixed times (4 and 1))
    private static Instance createSimpleInstance() {
        // 4 jobs, 2 machines
        int[] releaseDates = {0, 0, 0, 0};
        Instance instance = new Instance(4, 2, releaseDates);
        
        //processing times: tous à 4 pour simplifier
        for (int j = 0; j < 4; j++) {
            for (int m = 0; m < 2; m++) {
                instance.setProcessingTime(j, m, 4);
            }
        }
        
        //setup times => tous à 1 pour le test we don't care 
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int m = 0; m < 2; m++) {
                    instance.setSetupTime(i, j, m, 1);
                }
            }
        }
        
        return instance;
    }

    // Solution with 4 jobs on 2 machines:
    // M0: jobs 0, 1
    // M1: jobs 2, 3
    // job 4 is not scheduled to test edge cases
    // makespan should be 4 + 1 + 4 = 9
    // (4 for job 0, 1 for setup to job 1, 4 for job 1)
    private static Solution createSimpleSolution(Instance instance) {
        Solution solution = new Solution(instance);
        
        // Machine 0: jobs 0, 1
        solution.getSchedule(0).addJob(instance.getJob(0));
        solution.getSchedule(0).addJob(instance.getJob(1));
        
        // Machine 1: jobs 2, 3
        solution.getSchedule(1).addJob(instance.getJob(2));
        solution.getSchedule(1).addJob(instance.getJob(3));
        
        solution.calculateMakespan();
        return solution;
    }
    
    //tests
    private static void testInternalSwap(Solution solution) {
        RandomInternalSwap operator = new RandomInternalSwap(12345);
        
        System.out.println("before intern swap:");
        System.out.println("  M0: " + getJobIds(solution.getSchedule(0)));
        System.out.println("  M1: " + getJobIds(solution.getSchedule(1)));
        
        Solution neighbor = operator.apply(solution);
        
        System.out.println("\n after intern Swap:");
        System.out.println("  M0: " + getJobIds(neighbor.getSchedule(0)));
        System.out.println("  M1: " + getJobIds(neighbor.getSchedule(1)));
        
        //vérifs
        assert countJobs(neighbor) == 4 : "ERROR: not same nbr of jobs ";
        assert !solution.getSchedule(0).getJobSequence().equals(
                neighbor.getSchedule(0).getJobSequence()) ||
               !solution.getSchedule(1).getJobSequence().equals(
                neighbor.getSchedule(1).getJobSequence()) 
               : "ERROR: Nothing changed!";
        
        System.out.println("\n OK swapped on same machine");
    }
    
    private static void testExternalSwap(Solution solution) {
        RandomExternalSwap operator = new RandomExternalSwap(12345);

        System.out.println("before external swap:");
        System.out.println("  M0: " + getJobIds(solution.getSchedule(0)));
        System.out.println("  M1: " + getJobIds(solution.getSchedule(1)));
        
        Solution neighbor = operator.apply(solution);

        System.out.println("\n after external swap:");
        System.out.println("  M0: " + getJobIds(neighbor.getSchedule(0)));
        System.out.println("  M1: " + getJobIds(neighbor.getSchedule(1)));
        
        //vérifs
        assert countJobs(neighbor) == 4 : "ERROR: not same nbr of jobs ";
        assert neighbor.getSchedule(0).getJobCount() == 2 : "ERROR: M0 job count wrong!";
        assert neighbor.getSchedule(1).getJobCount() == 2 : "ERROR: M1 job count wrong!";

        System.out.println("\n OK swapped on different machine");
    }
    
    //utils functs

    //counts total jobs in the solution
    private static int countJobs(Solution sol) {
        int count = 0;
        for (int m = 0; m < sol.getNumberOfMachines(); m++) {
            count += sol.getSchedule(m).getJobCount();
        }
        return count;
    }
    
    //returns a string with the job ids in the schedule
    private static String getJobIds(Schedule schedule) {
        StringBuilder sb = new StringBuilder("[");
        for (Job job : schedule.getJobSequence()) {
            sb.append(job.getId()).append(" ");
        }
        if (sb.length() > 1) sb.setLength(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }
    
    //prints the solution with makespan and each machine's schedule
    private static void printSolution(Solution sol) {
        System.out.println("  Makespan: " + sol.getMakespan());
        for (int m = 0; m < sol.getNumberOfMachines(); m++) {
            Schedule s = sol.getSchedule(m);
            System.out.println("  M" + m + " (C=" + s.getCompletionTime() + "): " + getJobIds(s));
        }
    }
}