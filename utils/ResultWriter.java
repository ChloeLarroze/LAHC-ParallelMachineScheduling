package utils;

import java.io.*;
import solution.*;


public class ResultWriter {
    
    
    public static void printSolution(Solution solution) {
        System.out.println(solution.toString());
    }
    
    
    public static void printDetailedSolution(Solution solution) {
        System.out.println(solution.toDetailedString());
    }
    
    //writes solution to a txt file
    public static void writeToFile(Solution solution, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        
        try {
            writer.write("Makespan: " + solution.getMakespan());
            writer.newLine();
            writer.newLine();
            
            for (int m = 0; m < solution.getNumberOfMachines(); m++) {
                Schedule schedule = solution.getSchedule(m);
                writer.write(schedule.toString());
                writer.newLine();
            }
            
        } finally {
            writer.close();
        }
        
        System.out.println("Solution written to: " + filename);
    }
}
