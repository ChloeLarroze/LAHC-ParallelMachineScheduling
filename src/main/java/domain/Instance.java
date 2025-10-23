// ./domain/Instance.java
package domain;

// import domain.Job; //useless 2 import here (same package as this class)
// import domain.Machine; //sameeeee

/**
 * Contient tous les jobs, machines et paramètres de temps (traitement et setup).
 */
public class Instance {
    private final Job[] jobs;
    private final Machine[] machines;
    
    // processingTimes[j][k] = temps de traitement du job j sur la machine k (les p_jk)
    private final int[][] processingTimes;
    
    // setupTimes[i][j][k] = temps de setup entre job i et job j sur machine k (les s_ij^k)
    private final int[][][] setupTimes;
    
    private final int numJobs;
    private final int numMachines;
    
    //constructor
    public Instance(int numJobs, int numMachines) {
        this.numJobs = numJobs;
        this.numMachines = numMachines;
        
        //init jobs and machines arrays
        this.jobs = new Job[numJobs];
        this.machines = new Machine[numMachines];
        
        for (int i = 0; i < numJobs; i++) {
            this.jobs[i] = new Job(i, 0); //release date à définir après
        }
        
        for (int i = 0; i < numMachines; i++) {
            this.machines[i] = new Machine(i);
        }
        
        //init matrice temps 
        this.processingTimes = new int[numJobs][numMachines];
        this.setupTimes = new int[numJobs][numJobs][numMachines];
    }
    
    //constructor with release dates array and checks it matches numJobs
    public Instance(int numJobs, int numMachines, int[] releaseDates) {
        this(numJobs, numMachines);
        
        if (releaseDates.length != numJobs) {
            throw new IllegalArgumentException("Release dates array must match number of jobs");
        }
        
        //recréer les jobs avec les bonnes release dates
        for (int i = 0; i < numJobs; i++) {
            this.jobs[i] = new Job(i, releaseDates[i]);
        }
    }
    
    //getteers 
    public Job getJob(int index) {
        return jobs[index];
    }
    
    public Machine getMachine(int index) {
        return machines[index];
    }
    
    public int getNumberOfJobs() {
        return numJobs;
    }
    
    public int getNumberOfMachines() {
        return numMachines;
    }
    
    //gets time of processinfg and setup
    public int getProcessingTime(Job job, Machine machine) {
        return processingTimes[job.getId()][machine.getId()];
    }
    
    //if prevJob is null, we consider the initial setup (diagonal)
    public int getSetupTime(Job prevJob, Job nextJob, Machine machine) {
        // Si prevJob est null, on considère le setup initial (diagonal)
        int i = (prevJob == null) ? nextJob.getId() : prevJob.getId(); //thanks copilot for the shorter one liner vrsion :)
        int j = nextJob.getId();
        int k = machine.getId();
        return setupTimes[i][j][k];
    }
    
    //setters
    // we suppose jobId and machineId are valid
    public void setProcessingTime(int jobId, int machineId, int time) {
        processingTimes[jobId][machineId] = time;
    }

    // we suppose prevJobId, nextJobId and machineId are valid
    public void setSetupTime(int prevJobId, int nextJobId, int machineId, int time) {
        setupTimes[prevJobId][nextJobId][machineId] = time;
    }
    
    //toString 4 beautiful printing
    @Override
    public String toString() {
        return String.format("Instance[%d jobs, %d machines]", numJobs, numMachines);
    }
}