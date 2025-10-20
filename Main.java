import algo.heuristic.BIBAHeuristic;
import algo.metaheuristic.LAHCMetaheuristic;
import domain.Instance;
import domain.Job;
import solution.Schedule;
import solution.Solution;
import utils.Gantt;
import utils.InstanceReader;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║    LAHC - Parallel Machine Scheduling Problem              ║");
        System.out.println("║    R|rⱼ, sᵢⱼₖ|Cmax                                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        Instance instance = InstanceReader.readFromFile("./resources/Instance.txt");
        // Instance instance = InstanceReader.createPaperInstance(); //Instance du papier pour l'exemple, TODO à déplacer

        // Résoudre avec LAHC
        solveLAHC(instance);
    }


    /// FUNCTS =====================================================
    // Solve the instance using LAHC
    private static void solveLAHC(Instance instance) {

        //on crée LAHC
        BIBAHeuristic biba = new BIBAHeuristic();
        LAHCMetaheuristic lahc = new LAHCMetaheuristic(biba);

        //PARAMS & CONFIG
        System.out.println("Configuration LAHC:");
        System.out.println("  - Limite itérations: " + (instance.getNumberOfJobs() * instance.getNumberOfMachines() / 2));
        System.out.println("  - Limite non-amélioration: " + lahc.getNonImprovementLimit() + "\n");
        lahc.setMaxIterations(5); // TODO  : à passer en param
        
        System.out.println("=".repeat(60));
        System.out.println("DÉBUT RÉSOLUTION");
        System.out.println("=".repeat(60) + "\n"); //FIX make a pretty line ...
        
        //temps d'exécution
        long startTime = System.currentTimeMillis();
        
        // RÉSOUDRE
        Solution solution = lahc.solve(instance);
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // if (executionTime != maxExecutionTime) {
        // }
        
        // AFFICHER
        System.out.println("\n" + "=".repeat(60));
        System.out.println("RÉSULTATS FINAUX");
        System.out.println("=".repeat(60) + "\n");
        
        // 1. Solution détaillée
        printDetailedSolution(solution);
        
        // 2. Validation
        // System.out.println("\n" + "─".repeat(60));
        // System.out.println("VALIDATION");
        // System.out.println("─".repeat(60));
        // validateSolution(solution, instance);
        
        // 3. Statistiques
        System.out.println("\n" + "─".repeat(60));
        System.out.println("STATISTIQUES");
        System.out.println("─".repeat(60));
        System.out.println("  Temps d'exécution: " + executionTime + " ms");
        System.out.println("  Jobs: " + instance.getNumberOfJobs());
        System.out.println("  Machines: " + instance.getNumberOfMachines());
        System.out.println("  Makespan final: " + solution.getMakespan());

        // 4. Gantt
        System.out.println("\n" + "─".repeat(60));
        System.out.println("DIAGRAMME DE GANTT");
        System.out.println("─".repeat(60) + "\n");
        Gantt.printGantt(solution);
    }
    
    //print helper funct 
    private static void printDetailedSolution(Solution solution) {
        System.out.println("SOLUTION OBTENUE:");
        System.out.println("  Makespan (Cmax): " + solution.getMakespan() + "\n");
        
        for (int m = 0; m < solution.getNumberOfMachines(); m++) {
            Schedule schedule = solution.getSchedule(m);
            System.out.printf("Machine %d (Completion: %d):%n", m, schedule.getCompletionTime());
            
            if (schedule.getJobCount() == 0) {
                System.out.println("  [Vide]");
            } else {
                System.out.println("  Séquence: " + formatJobSequence(schedule));
                
                //détails de chaque job
                for (Job job : schedule.getJobSequence()) {
                    int start = schedule.getStartTime(job);
                    int end = schedule.getEndTime(job);
                    System.out.printf("    J%d: [%d → %d] (durée: %d)%n",
                                    job.getId(), start, end, end - start);
                }
            }
            System.out.println();
        }
    }

    //function that formats the job sequence for printing aka does J0 J1 J2 ... thks copilot
    private static String formatJobSequence(Schedule schedule) {
        if (schedule.getJobCount() == 0) return "[]";
        
        StringBuilder sb = new StringBuilder("[");
        for (Job job : schedule.getJobSequence()) {
            sb.append("J").append(job.getId()).append(" ");
        }
        sb.setLength(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }
}
