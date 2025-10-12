package algo.heuristic;

import domain.*;
import solution.*;

public interface Heuristic {
    //Construit une solution initiale pour l'instance donnée
    Solution buildInitialSolution(Instance instance);
}