package algo.heuristic;

import domain.*;

//Classe auxiliaire pour représenter un mouvement d'insertion.
public class InsertionMove {
    public final Job job;
    public final Machine machine;
    public final int makespan;
    
    public InsertionMove(Job job, Machine machine, int makespan) {
        this.job = job;
        this.machine = machine;
        this.makespan = makespan;
    }
}