package solution;

import domain.*;
import java.util.*;

//ordonnancement détaillé d'une machine.
public class Schedule {
    private final Machine machine;
    private final List<Job> jobSequence;
    private final Instance instance;
    
    private int completionTime;
    private final Map<Job, Integer> startTimes;
    private final Map<Job, Integer> endTimes;
    
    //empty schedule constructor
    public Schedule(Machine machine, Instance instance) {
        this.machine = machine;
        this.instance = instance;
        this.jobSequence = new ArrayList<>();
        this.startTimes = new HashMap<>();
        this.endTimes = new HashMap<>();
        this.completionTime = 0;
    }
    
    //adds a job at the end of the sequence
    public void addJob(Job job) {
        jobSequence.add(job);
        calculateSchedule();
    }
    
    //inserts a job at a specific position
    public void addJob(Job job, int position) {
        if (position < 0 || position > jobSequence.size()) {
            throw new IndexOutOfBoundsException("Invalid position: " + position); //position can be size (add at end
        }
        jobSequence.add(position, job);
        calculateSchedule();
    }
    
    //removes a job by reference
    public boolean removeJob(Job job) {
        boolean removed = jobSequence.remove(job);
        if (removed) {
            calculateSchedule();
        }
        return removed;
    }

    //removes a job at a specific position
    public Job removeJobAt(int position) {
        if (position < 0 || position >= jobSequence.size()) {
            throw new IndexOutOfBoundsException("Invalid position: " + position);
        }
        Job removed = jobSequence.remove(position);
        calculateSchedule();
        return removed;
    }
    
    //swaps two jobs at given positions
    public void swapJobs(int pos1, int pos2) {
        if (pos1 < 0 || pos1 >= jobSequence.size() || 
            pos2 < 0 || pos2 >= jobSequence.size()) {
            throw new IndexOutOfBoundsException("Invalid swap positions");
        }
        
        Collections.swap(jobSequence, pos1, pos2);
        calculateSchedule();
    }
    
    //recalculates the schedule (start times, end times, completion time)
    public void calculateSchedule() {
        startTimes.clear();
        endTimes.clear();
        
        if (jobSequence.isEmpty()) {
            completionTime = 0;
            return;
        }
        
        int currentTime = 0;
        Job previousJob = null;
        
        for (Job job : jobSequence) {
            //le setup commence après la fin du job précédent
            int setupStartTime = Math.max(currentTime, job.getReleaseDate());
            
            //get setup and processing times
            int setupTime = instance.getSetupTime(previousJob, job, machine);
            int processingTime = instance.getProcessingTime(job, machine);
            
            //p_jk starts after setup completes FIX here
            int processingStartTime = setupStartTime + setupTime;
            
            //record time(startTime is when processing begins)
            startTimes.put(job, processingStartTime);
            int endTime = processingStartTime + processingTime;
            endTimes.put(job, endTime);
            
            //update pour le next job
            currentTime = endTime;
            previousJob = job;
        }

        //when the last job finishes => completionTime est returned 
        completionTime = currentTime;
    }
    
    // === Getters ===
    public Machine getMachine() {
        return machine;
    }
    
    public List<Job> getJobSequence() {
        return new ArrayList<>(jobSequence); //copie pour éviter modification externe
    }
    
    public int getCompletionTime() {
        return completionTime;
    }
    
    public int getJobCount() {
        return jobSequence.size();
    }
    
    public Integer getStartTime(Job job) {
        return startTimes.get(job);
    }
    
    public Integer getEndTime(Job job) {
        return endTimes.get(job);
    }

    //copy of the schedule
    public Schedule copy() {
        Schedule copy = new Schedule(this.machine, this.instance);
        copy.jobSequence.addAll(this.jobSequence);
        copy.calculateSchedule();
        return copy;
    }
    

    //toString for easy printing
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(machine).append(": ");
        if (jobSequence.isEmpty()) {
            sb.append("[]");
        } else {
            sb.append("[");
            for (Job job : jobSequence) {
                sb.append("J").append(job.getId()).append(" ");
            }
            sb.setLength(sb.length() - 1); //rm last space //TODO maybe use join
            sb.append("]");
        }
        sb.append(" (C=").append(completionTime).append(")");
        return sb.toString();
    }
}
