package algo.neighborhood;

import domain.Job;
import solution.*;
import java.util.*;

//opérateur de voisinage qui échange aléatoirement deux jobs entre deux machines différentes
public class RandomExternalSwap implements NeighborhoodOperator {
    private final Random random;
    
    // 2 constructors
    // un avec seed pour les tests
    // un sans seed pour l'utilisation normale
    public RandomExternalSwap() {
        this.random = new Random();
    }
    
    public RandomExternalSwap(long seed) {
        this.random = new Random(seed);
    }
    
    // apply the operator to generate a neighboring solution
    @Override
    public Solution apply(Solution solution) {
        Solution neighbor = solution.copy();
        
        // machines qui ont au moins 1 job => on peut échanger
        List<Integer> nonEmptyMachines = new ArrayList<>();
        for (int m = 0; m < neighbor.getNumberOfMachines(); m++) {
            if (neighbor.getSchedule(m).getJobCount() > 0) {
                nonEmptyMachines.add(m);
            }
        }
        
        //or on a besoin de 2 machines non vides
        if (nonEmptyMachines.size() < 2) {
            return neighbor;
        }
        
        // on prend deux machines différentes de manièere random
        int machine1Id = nonEmptyMachines.get(random.nextInt(nonEmptyMachines.size()));
        int machine2Id;
        //structure avec le do while pour être sûr que les deux machines sont différentes src: https://stackoverflow.com/questions/196017/unique-random-numbers-in-java
        do {
            machine2Id = nonEmptyMachines.get(random.nextInt(nonEmptyMachines.size()));
        } while (machine1Id == machine2Id);
        
        Schedule schedule1 = neighbor.getSchedule(machine1Id);
        Schedule schedule2 = neighbor.getSchedule(machine2Id);
        
        //choix du job aléatoire de chaque machine
        int pos1 = random.nextInt(schedule1.getJobCount());
        int pos2 = random.nextInt(schedule2.getJobCount());
        
        Job job1 = schedule1.getJobSequence().get(pos1);
        Job job2 = schedule2.getJobSequence().get(pos2);
        
        //swap
        schedule1.removeJobAt(pos1);
        schedule2.removeJobAt(pos2);
        
        schedule1.addJob(job2, pos1);
        schedule2.addJob(job1, pos2);
        
        neighbor.invalidate();
        
        return neighbor;
    }
}