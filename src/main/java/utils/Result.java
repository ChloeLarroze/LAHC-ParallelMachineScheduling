package utils;

//classe pour stocker les résultats d'une exécution
public class Result {
    private final String instanceName;
    private final int makespan;
    private final long computationTime;
    private final int iterations;
    
    public Result(String instanceName, int makespan, long computationTime, int iterations) {
        this.instanceName = instanceName;
        this.makespan = makespan;
        this.computationTime = computationTime;
        this.iterations = iterations;
    }
    
    public String getInstanceName() {
        return instanceName;
    }
    
    public int getMakespan() {
        return makespan;
    }
    
    public long getComputationTime() {
        return computationTime;
    }
    
    public int getIterations() {
        return iterations;
    }
    
    @Override
    public String toString() {
        return String.format("Result[%s: Cmax=%d, Time=%dms, Iter=%d]",
                            instanceName, makespan, computationTime, iterations);
    }
}
