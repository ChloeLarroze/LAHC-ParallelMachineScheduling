package solution;

public class Solution {
    private final Schedule schedule;
    private final int totalCompletionTime;

    //constructor
    public Solution(Schedule schedule, int totalCompletionTime) {
        this.schedule = schedule;
        this.totalCompletionTime = totalCompletionTime;
    }

    //getters
    public Schedule getSchedule() {
        return schedule;
    }

    public int getTotalCompletionTime() {
        return totalCompletionTime;
    }
    
}
