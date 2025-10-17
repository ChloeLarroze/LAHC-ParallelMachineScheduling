package utils;

import domain.*;
import java.io.*;
import java.util.*;


public class InstanceReader {
    
    /**
     * Lit une instance depuis un fichier txt
     * 
     * Format du fichier :
     * Ligne 1: numJobs numMachines
     * Ligne 2: releaseDates (r0 r1 r2 ...)
     * Lignes suivantes: processingTimes (matrice n x m)
     * Lignes suivantes: setupTimes pour chaque machine (matrices n x n x m)
     */
    public static Instance readFromFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        
        try {
            //dimensions
            String[] dims = reader.readLine().trim().split("\\s+");
            int numJobs = Integer.parseInt(dims[0]);
            int numMachines = Integer.parseInt(dims[1]);
            
            //release dates
            String[] rdates = reader.readLine().trim().split("\\s+");
            int[] releaseDates = new int[numJobs];
            for (int i = 0; i < numJobs; i++) {
                releaseDates[i] = Integer.parseInt(rdates[i]);
            }
            
            Instance instance = new Instance(numJobs, numMachines, releaseDates);
            
            //reading processing times (n lignes de m valeurs)
            for (int j = 0; j < numJobs; j++) {
                String[] times = reader.readLine().trim().split("\\s+");
                for (int m = 0; m < numMachines; m++) {
                    instance.setProcessingTime(j, m, Integer.parseInt(times[m]));
                }
            }
            
            //reading setup times pour chaque machine
            for (int m = 0; m < numMachines; m++) {
                reader.readLine(); //FIX 
                
                for (int i = 0; i < numJobs; i++) {
                    String[] times = reader.readLine().trim().split("\\s+");
                    for (int j = 0; j < numJobs; j++) {
                        instance.setSetupTime(i, j, m, Integer.parseInt(times[j]));
                    }
                }
            }
            
            return instance;
            
        } finally {
            reader.close();
        }
    }
    
    //random instance generator thks to the paper's protocol & copilot
    public static Instance createRandomInstance(int numJobs, int numMachines,  int maxProcessingTime, int maxSetupTime, double releaseFactor) 
        {
        Random random = new Random();
        
        // generates release dates selon le protocole du papier
        int[] releaseDates = new int[numJobs];
        double rho = 0; // Average processing time
        
        // D'abord calculer rho
        for (int j = 0; j < numJobs; j++) {
            for (int m = 0; m < numMachines; m++) {
                rho += random.nextInt(maxProcessingTime) + 1;
            }
        }
        rho /= (numJobs * numMachines);

        //on calcule L selon la formule du papier
        int L = (int) Math.ceil((numJobs * rho * releaseFactor) / numMachines);

        // les release dates
        for (int j = 0; j < numJobs; j++) {
            releaseDates[j] = L > 0 ? random.nextInt(L + 1) : 0;
        }
        
        Instance instance = new Instance(numJobs, numMachines, releaseDates);

        // les processing times
        for (int j = 0; j < numJobs; j++) {
            for (int m = 0; m < numMachines; m++) {
                instance.setProcessingTime(j, m, random.nextInt(maxProcessingTime) + 1);
            }
        }
        
        // Générer setup times
        for (int i = 0; i < numJobs; i++) {
            for (int j = 0; j < numJobs; j++) {
                for (int m = 0; m < numMachines; m++) {
                    instance.setSetupTime(i, j, m, random.nextInt(maxSetupTime) + 1);
                }
            }
        }
        
        return instance;
    }
    
    //instance papier (déplacée depuis Main.java)
    public static Instance createPaperInstance() {
        System.out.println("exemple du papier\n");
        
        //r_j(Table 1)
        int[] releaseDates = {3, 5, 1, 0, 3};
        Instance instance = new Instance(5, 2, releaseDates);
        
        // p_ij (Table 1 as well)
        int[][] processingTimes = {
            {2, 2}, // Job 0: p01=2, p02=2
            {1, 4}, // Job 1: p11=1, p12=4
            {2, 5}, // Job 2: p21=2, p22=5
            {6, 2}, // Job 3: p31=6, p32=2
            {4, 1}  // Job 4: p41=4, p42=1
        };
        
        for (int j = 0; j < 5; j++) {
            for (int m = 0; m < 2; m++) {
                instance.setProcessingTime(j, m, processingTimes[j][m]);
            }
        }
        
        //s_ij1 (Table 2, left)
        int[][] setupM0 = { 
            {9, 5, 1, 2, 3},
            {7, 3, 5, 1, 6},
            {3, 5, 7, 2, 7},
            {5, 3, 3, 1, 2},
            {1, 1, 2, 2, 3}
        };
        
        // s_ij2 (Table 2, right)
        int[][] setupM1 = {
            {1, 10, 8, 3, 4},
            {4, 2, 2, 1, 5},
            {2, 1, 2, 5, 2},
            {5, 2, 1, 4, 3},
            {4, 6, 1, 3, 1}
        };
        
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                instance.setSetupTime(i, j, 0, setupM0[i][j]);
                instance.setSetupTime(i, j, 1, setupM1[i][j]);
            }
        }
        
        System.out.println("OK - instance créée");
        return instance;
    }
}

// ========================================