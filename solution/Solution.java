package solution;

import domain.*;
import java.util.*;

//une solution est composée d'un ensemble de schedules, un par machine
//elle permet de calculer le makespan et d'identifier les machines goulot
public class Solution {
    private final Schedule[] schedules;
    private final Instance instance;
    private int makespan;
    private boolean evaluated;
    
   //constructor only with instance
    public Solution(Instance instance) {
        this.instance = instance;
        this.schedules = new Schedule[instance.getNumberOfMachines()];
        
        //init schedule vide pour chaque machine
        for (int i = 0; i < instance.getNumberOfMachines(); i++) {
            schedules[i] = new Schedule(instance.getMachine(i), instance);
        }
        
        this.makespan = 0;
        this.evaluated = false;
    }
    
    // getters 
    public Schedule getSchedule(int machineId) {
        if (machineId < 0 || machineId >= schedules.length) {
            throw new IndexOutOfBoundsException("Invalid machine ID: " + machineId);
        }
        return schedules[machineId];
    }
    
    public Schedule getSchedule(Machine machine) {
        return getSchedule(machine.getId());
    }
    
    //calculates the makespan by evaluating each schedule
    //sets evaluated to true
    public void calculateMakespan() {
        makespan = 0;
        for (Schedule schedule : schedules) {
            schedule.calculateSchedule();
            makespan = Math.max(makespan, schedule.getCompletionTime());
        }
        evaluated = true;
    }
    
    //yet another getter that we'll need each time
    public int getMakespan() {
        if (!evaluated) {
            calculateMakespan();
        }
        return makespan;
    }

    //identifies bottleneck machines aka those with completion time == makespan
    public List<Machine> getBottleneckMachines() {
        int currentMakespan = getMakespan();
        List<Machine> bottlenecks = new ArrayList<>();
        
        for (Schedule schedule : schedules) {
            if (schedule.getCompletionTime() == currentMakespan) {
                bottlenecks.add(schedule.getMachine());
            }
        }
        
        return bottlenecks;
    }
    
    //marks the solution as needing reevaluation
    public void invalidate() {
        evaluated = false;
    }
    
    //same as in schedule, just copies in case 
    public Solution copy() {
        Solution copy = new Solution(this.instance);
        
        // chaque schedule
        for (int i = 0; i < schedules.length; i++) {
            copy.schedules[i] = this.schedules[i].copy();
        }
        
        copy.makespan = this.makespan;
        copy.evaluated = this.evaluated;
        
        return copy;
    }
    
    // === Other getters ===
    
    public Instance getInstance() {
        return instance;
    }
    
    public int getNumberOfMachines() {
        return schedules.length;
    }
    
    public boolean isEvaluated() {
        return evaluated;
    }
    
    public int getTotalJobCount() {
        int count = 0;
        for (Schedule schedule : schedules) {
            count += schedule.getJobCount();
        }
        return count;
    }
    
    // checks if all jobs are scheduled
    public boolean isComplete() {
        return getTotalJobCount() == instance.getNumberOfJobs();
    }
    
    //string representation
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Solution (Cmax=").append(getMakespan()).append("):\n");
        for (Schedule schedule : schedules) {
            sb.append("  ").append(schedule).append("\n");
        }
        return sb.toString();
    }
    
    //detailed string representation with job timings
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Solution Details ===\n");
        sb.append("Makespan: ").append(getMakespan()).append("\n\n");
        
        for (int m = 0; m < schedules.length; m++) {
            Schedule schedule = schedules[m];
            sb.append(schedule.getMachine()).append(" (Completion: ")
              .append(schedule.getCompletionTime()).append("):\n");
            
            if (schedule.getJobCount() == 0) {
                sb.append("  [Empty]\n");
            } else {
                for (Job job : schedule.getJobSequence()) {
                    int start = schedule.getStartTime(job);
                    int end = schedule.getEndTime(job);
                    sb.append(String.format("  %s: [%d -> %d] (duration: %d)\n",
                                          job, start, end, end - start));
                }
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
}
