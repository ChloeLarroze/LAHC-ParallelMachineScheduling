package algo.metaheuristic;


import  domain.*;
import  solution.*;
import  algo.heuristic.*;
import  algo.localsearch.*;
import  algo.neighborhood.*;
import java.util.*;



public class LAHCMetaheuristic {
    private final Heuristic heuristic;
    private final LocalSearch localSearch;
    private final List<NeighborhoodOperator> operators;
    private final Random random;
    
    // Paramètres LAHC
    private int[] historyList;
    private int historyLength; // LH
    private int maxIterations;
    private int nonImprovementLimit;
    
    // Solutions
    private Solution bestSolution;
    private Solution currentSolution;
    
    // stats 
    private int iterationCount;
    private int lastImprovementIteration;
    
    //default constructor with heuristic and default history length
    public LAHCMetaheuristic(Heuristic heuristic) {
        this(heuristic, 30); // LH = 30 selon le papier
    }
    
    //constructor with heuristic and history length
    public LAHCMetaheuristic(Heuristic heuristic, int historyLength) {
        this.heuristic = heuristic;
        this.localSearch = new LocalSearch();
        this.historyLength = historyLength;
        this.nonImprovementLimit = 1000;
        this.random = new Random();
        
        // opérateurs de voisinage (50%-50%)
        this.operators = new ArrayList<>();
        this.operators.add(new RandomInternalSwap());
        this.operators.add(new RandomExternalSwap());
    }
    

    public Solution solve(Instance instance) {
        // 1. Générer la solution initiale avec l'heuristique
        System.out.println("Generating initial solution with BIBA heuristic...");
        currentSolution = heuristic.buildInitialSolution(instance);
        bestSolution = currentSolution.copy(); //initial best solution = initial solution
        
        int initialMakespan = bestSolution.getMakespan();
        System.out.println("Initial makespan: " + initialMakespan);
        
        // 2. Initialiser la liste historique
        historyList = new int[historyLength];
        Arrays.fill(historyList, initialMakespan);
        
        // 3. Calculer le nombre maximum d'itérations si non défini
        if (maxIterations == 0) {
            // dépend de la taille de l'instance
            maxIterations = instance.getNumberOfJobs() * instance.getNumberOfMachines() * 100; // ajustable TODO ? 
        }
        
        // 4. Boucle principale LAHC
        iterationCount = 0;
        lastImprovementIteration = 0;
        int nonImprovementCount = 0;
        
        System.out.println("LAHC with " + maxIterations + " max iterations...");
        
        while (iterationCount < maxIterations && nonImprovementCount < nonImprovementLimit) {
            iterationCount++;
            
            // a. Générer une solution voisine
            NeighborhoodOperator operator = operators.get(random.nextInt(operators.size())); //retourne soit 0 soit 1, donc 50%-50%
            //System.out.println("Using operator: " + operator.getClass().getSimpleName()); //debug
            Solution neighbor = operator.apply(currentSolution);
            
            // b. Améliorer avec recherche locale
            neighbor = localSearch.improve(neighbor);
            
            int currentCost = currentSolution.getMakespan();
            int neighborCost = neighbor.getMakespan();
            
            // c. Critère d'acceptation LAHC
            int historyIndex = iterationCount % historyLength;
            int historyCost = historyList[historyIndex];

            if (neighborCost <= currentCost || neighborCost < historyCost) { //we could have used an "acceptSolution" method but whatever
                currentSolution = neighbor;
            }
            
            // d. Mettre à jour la meilleure solution
            if (neighborCost < bestSolution.getMakespan()) {
                bestSolution = neighbor.copy();
                lastImprovementIteration = iterationCount;
                nonImprovementCount = 0;
                
                if (iterationCount % 100 == 0) {
                    System.out.printf("Iteration %d: New best makespan = %d%n", 
                                    iterationCount, neighborCost);
                }
            } else {
                nonImprovementCount++;
            }
            
            // e. Mettre à jour la liste historique
            historyList[iterationCount % historyLength] = neighborCost;
            
            // Affichage périodique
            if (iterationCount % 1000 == 0) {
                System.out.printf("Iteration %d / %d - Best: %d, Current: %d%n",
                                iterationCount, maxIterations, 
                                bestSolution.getMakespan(), currentCost);
            }
        }
        
        System.out.println("\nLAHC finished:");
        System.out.println("  Total iterations: " + iterationCount);
        System.out.println("  Last improvement at iteration: " + lastImprovementIteration);
        System.out.println("  Initial makespan: " + initialMakespan);
        System.out.println("  Final makespan: " + bestSolution.getMakespan());
        System.out.printf("  Improvement: %.2f%%%n", 
                        100.0 * (initialMakespan - bestSolution.getMakespan()) / initialMakespan);
        
        return bestSolution;
    }
}