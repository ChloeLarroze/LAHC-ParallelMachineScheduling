//interface for neighborhood operators
// ./algo/neighborhood/NeighborhoodOperator.java

package algo.neighborhood;

import solution.Solution;

public interface NeighborhoodOperator {
    //on applique l'opérateur pour générer une solution voisine.
    Solution apply(Solution solution);
}