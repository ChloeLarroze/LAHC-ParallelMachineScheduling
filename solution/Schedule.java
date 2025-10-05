package solution;

public class Schedule {
    private final Instance instance;
    private final int[] jobOrder;

    //constructor
    public Schedule(Instance instance, int[] jobOrder) {
        this.instance = instance;
        this.jobOrder = jobOrder;
    }

    //getters
    public Instance getInstance() {
        return instance;
    }

    public int[] getJobOrder() {
        return jobOrder;
    }
}
