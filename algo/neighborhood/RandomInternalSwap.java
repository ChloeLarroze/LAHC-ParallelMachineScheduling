package algo.neighborhood;

import java.util.*;
import solution.*;

public class RandomInternalSwap implements NeighborhoodOperator { //échange aléatoire de deux jobs sur une même machine
    //implements bc it's a neighborhood operator
    private final Random random; //final bc we don't want to change it after initialization https://stackoverflow.com/questions/12301712/why-declare-random-as-final-in-java
    
    //constructors
    public RandomInternalSwap() {
        this.random = new Random();
    }
    
    public RandomInternalSwap(long seed) {
        this.random = new Random(seed);
    }
    
    // apply the operator to generate a neighboring solution
    @Override
    public Solution apply(Solution solution) {
        Solution neighbor = solution.copy();
        
        //we choose a random machine with at least 2 jobs
        List<Integer> candidateMachines = new ArrayList<>(); //list of machine ids with at least 2 jobs
        for (int m = 0; m < neighbor.getNumberOfMachines(); m++) 
        {
            if (neighbor.getSchedule(m).getJobCount() >= 2)  //2 jobs 
            {
                candidateMachines.add(m);
            }
        }
        // if no machine has at least 2 jobs, return the same solution
        if (candidateMachines.isEmpty()) 
        {
            return neighbor;
        }
        
        // we choose a random machine among the candidates
        int machineId = candidateMachines.get(random.nextInt(candidateMachines.size())); //random machine id from the candidates
        Schedule schedule = neighbor.getSchedule(machineId);
        
        // we choose two different random positions
        int jobCount = schedule.getJobCount();
        int pos1 = random.nextInt(jobCount);
        int pos2 = random.nextInt(jobCount);
        
        // we maeke sure that the positions are different
        while (pos1 == pos2 && jobCount > 1) {
            pos2 = random.nextInt(jobCount);
        }

        // we perform the swap
        schedule.swapJobs(pos1, pos2);
        neighbor.invalidate();
        
        return neighbor;
    }
}
