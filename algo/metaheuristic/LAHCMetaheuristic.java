package algo.metaheuristic;


import  algo.heuristic.*;
import  algo.localsearch.*;
import  algo.neighborhood.*;
import  domain.*;
import  java.util.*;
import solution.*;



public class LAHCMetaheuristic {
    private final Heuristic heuristic;
    private final LocalSearch localSearch;
    private final List<NeighborhoodOperator> operators;
    private final Random random;
    
    // Paramètres LAHC
    private int[] historyList;
    private int historyLength; // LH
    private int maxIterations; //todo ajustable ?
    private int nonImprovementLimit;
    
    // Solutions
    private Solution bestSolution;
    private Solution currentSolution;
    
    // stats 
    private int iterationCount;
    private int lastImprovementIteration;
    private int initialMakespan;
    private int bestMakespan;

    //default constructor with heuristic and default history length
    public LAHCMetaheuristic(Heuristic heuristic) {
        this(heuristic, 30); // LH = 30 selon le papier //PARAM  TODO ajustable ?
    }
    
    //constructor with heuristic and history length
    public LAHCMetaheuristic(Heuristic heuristic, int historyLength) {
        this.heuristic = heuristic;
        this.localSearch = new LocalSearch();
        this.historyLength = historyLength; // LH
        this.nonImprovementLimit = 1000; //by default in the paper 
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

        initialMakespan = bestSolution.getMakespan();
        bestMakespan = initialMakespan;
        System.out.println("Initial makespan: " + initialMakespan);
        
        // 2. Initialiser la liste historique
        historyList = new int[historyLength];
        Arrays.fill(historyList, initialMakespan);

        //FIX with iteration time rather than max time n*m/2 *1000 
        long timeLimitMs = (long) (instance.getNumberOfJobs() * instance.getNumberOfMachines() / 2.0 * 1000 ); //in ms  
        long startTime = System.currentTimeMillis();
        

        // 3. Boucle principale LAHC
        iterationCount = 0;
        lastImprovementIteration = 0;
        int nonImprovementCount = 0;

        //FIX MaxIter discrete or time-based ? TODO + add as a param 
        //while (iterationCount < maxIterations && nonImprovementCount < nonImprovementLimit)
        while ((System.currentTimeMillis() - startTime) < timeLimitMs  && nonImprovementCount < nonImprovementLimit)  //&& iterationCount < maxIterations
        {
            iterationCount++;
            //debug 
            System.out.println("Iteration " + iterationCount + ": " + (System.currentTimeMillis() - startTime) + " ms");

            // a. Générer une solution voisine
            NeighborhoodOperator operator = operators.get(random.nextInt(operators.size())); //retourne soit 0 soit 1, donc 50%-50%
            //System.out.println("Using operator: " + operator.getClass().getSimpleName()); //debug

            Solution neighbor = operator.apply(currentSolution);
            
            // b. Améliorer avec recherche locale
            //neighbor = localSearch.improve(neighbor); //ISSUE HERE -> stuck in local optimum every time

            //only improve some of neighbors to save time //FIX 
            if (random.nextDouble() < 1.0) { //always for now
                neighbor = localSearch.improve(neighbor);
            }
            
            int currentCost = currentSolution.getMakespan();
            int neighborCost = neighbor.getMakespan();
            
            // c. Critère d'acceptation LAHC
            int historyIndex = iterationCount % historyLength;
            int historyCost = historyList[historyIndex];

            if (neighborCost <= currentCost || neighborCost < historyCost) { //we could have used an "acceptSolution" method but whatever
                //neighbor = localSearch.improve(neighbor); //FIX either only on the accepted or on some of em 
                currentSolution = neighbor;
            }
            
            // d. Mettre à jour la meilleure solution
            if (neighborCost < bestSolution.getMakespan()) {
                bestSolution = neighbor.copy();
                bestMakespan = neighborCost;
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
            historyList[iterationCount % historyLength] = currentSolution.getMakespan();
            //historyList[iterationCount % historyLength] = neighborCost;
            
            // Affichage périodique
            if (iterationCount % 1000 == 0) {
                System.out.printf("Iteration %d / %d - Best: %d, Current: %d%n",
                                iterationCount, maxIterations, 
                                bestSolution.getMakespan(), currentCost);
            }
        }
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("\nLAHC finished:");
        System.out.println("  Total iterations: " + iterationCount);
        System.out.println("  Total time: " + (totalTime / 1000.0) + " s");
        System.out.println("  Last improvement at iteration: " + lastImprovementIteration);
        System.out.println("  Initial makespan: " + initialMakespan);
        System.out.println("  Final makespan: " + bestSolution.getMakespan());
        System.out.printf("  Improvement: %.2f%%%n", 
                        100.0 * (initialMakespan - bestSolution.getMakespan()) / initialMakespan);
        
        return bestSolution;
    }

    //setters 
    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public void setNonImprovementLimit(int limit) {
        this.nonImprovementLimit = limit;
    }

    public Solution getBestSolution() {
        return bestSolution;
    }

    public Solution getCurrentSolution() {
        return currentSolution;
    }

    public int getNonImprovementLimit() {
        return nonImprovementLimit;
    }

    public int getInitialMakespan() {
        return initialMakespan;
    }

    public int getBestMakespan() {
        return bestMakespan;
    }

    //make it adjustable (delete from constructor) TODO 
    // public void setNonImprovementLimit(int nonImprovementLimit) {
    //     this.nonImprovementLimit = nonImprovementLimit;
    // }

    //stats getters
    public int getIterationCount() {
        return iterationCount;
    }

    public int getLastImprovementIteration() {
        return lastImprovementIteration;
    }
}