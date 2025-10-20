package utils;

import solution.*;
import domain.*;


public class Gantt {

    public static void printGantt(Solution solution) {    

        int makespan = solution.getMakespan();
        int scale = Math.max(1, makespan / 50);
        
        System.out.println("Légende: ░=s_ijk | ▓=p_ij | ·=r_j\n");
        
        for (int m = 0; m < solution.getNumberOfMachines(); m++) {
            Schedule schedule = solution.getSchedule(m);
            System.out.printf("M%d: ", m);
            
            int currentTime = 0;
            Job previousJob = null;
            
            for (Job job : schedule.getJobSequence()) {
                Instance instance = solution.getInstance();
                Machine machine = schedule.getMachine();
                
                //release date
                int startTime = Math.max(currentTime, job.getReleaseDate());
                if (startTime > currentTime) {
                    int r_j = startTime - currentTime;
                    printChar('·', Math.max(1, r_j / scale));
                }
                
                //Setup time
                int s_ijk = instance.getSetupTime(previousJob, job, machine);
                printChar('░', Math.max(1, s_ijk / scale));

                // Processing time
                int p_ij = instance.getProcessingTime(job, machine);
                System.out.print("[J" + job.getId() + ":");
                printChar('▓', Math.max(1, p_ij / scale));
                System.out.print("]");

                currentTime = startTime + s_ijk + p_ij;
                previousJob = job;
            }
            
            System.out.printf(" (C=%d)", schedule.getCompletionTime());
            if (schedule.getCompletionTime() == makespan) {
                System.out.print(" ← BOTTLENECK"); //which machine is the bottleneck one 
            }
            System.out.println();
        }
        
        System.out.println("\nMakespan (Cmax) = " + makespan);
    }


    //affiche un caractère nX 
    private static void printChar(char c, int times) {
        for (int i = 0; i < times; i++) {
            System.out.print(c);
        }
    }
}