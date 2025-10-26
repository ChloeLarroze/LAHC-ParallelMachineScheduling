package algo.localsearch;

import domain.*;
import java.util.*;
import solution.*;

/**
 * Classe de recherche locale avec 5 opérateurs d'amélioration.
 * Sur le papier, leur fonctionnement est pas mal similaire entre eux aka chaque opérateur tente d'améliorer la solution en place.
 * 
 * Les 5 opérateurs sont appliqués pour améliorer une solution, comme décrit dans le papier :
 * 1. Bottleneck Internal Swap
 * 2. Bottleneck External Insertion
 * 3. Bottleneck External Swap
 * 4. Balancing
 * 5. Inter Machine Insertion (avec l'équation (1) du papier)
 */

public class LocalSearch {
    
    //retourne la solution améliorée à partir de la solution initiale
    //applique les opérateurs jusqu'à ce qu'aucune amélioration ne soit trouvée de ce que j'ai compris du papier
    public Solution improve(Solution solution) {
        Solution improved = solution.copy();
        boolean improvement;
        int iter = 0;
        int lastMakespan = improved.getMakespan();
        
        do {
            improvement = applyOperators(improved);
            int newMakespan = improved.getMakespan();
            iter++;

            //debug 
            //System.out.printf("[LocalSearch] Iter %d → Makespan: %d (Δ=%d)%n", iter, newMakespan, lastMakespan - newMakespan);

            // if (newMakespan >= lastMakespan && !improvement) {
            //     System.out.println("[LocalSearch] No further improvement found.");
            // }

            lastMakespan = newMakespan;

            // Sécurité : limite de cycles pour éviter boucle infinie
            if (iter > 1000) {
                //System.out.println("[LocalSearch] Stuck in local loop — forcing exit");
                break;
            }

        } while (improvement);
        
        return improved;
    }

    
    //applique les 5 opérateurs dans l'ordre et retourne true si au moins un a amélioré la solution
    private boolean applyOperators(Solution solution) {
        boolean improved = false;

        //we'll use |= to accumulate improvements from each operator
        //cause if an operator improves the solution, we want to keep that improvement
        improved |= bottleneckInternalSwap(solution); //si true, on a amélioré
        improved |= bottleneckExternalInsertion(solution); //idem 
        improved |= bottleneckExternalSwap(solution); //idem
        improved |= balancing(solution);
        improved |= interMachineInsertion(solution);
        
        return improved;
    }


    //==========================================================================================
    //opérateur 1 : Bottleneck Internal Swap
    //échange deux jobs sur une machine goulot pour réduire son temps de complétion
    //retourne true si une amélioration a été trouvée
    //modifie la solution en place
    //on parcourt toutes les machines goulot et on teste tous les swaps possibles
    //on garde le meilleur swap et on l'applique
    //on répète jusqu'à ce qu'aucun swap n'améliore la solution
    //on retourne true si au moins un swap a amélioré la solution
    //sinon on retourne false
    public boolean bottleneckInternalSwap(Solution solution) {
        int currentMakespan = solution.getMakespan();
        List<Machine> bottlenecks = solution.getBottleneckMachines();
        
        boolean improved = false;
        
        //pour chaque machine "goulot"
        for (Machine machine : bottlenecks) {
            Schedule schedule = solution.getSchedule(machine);
            List<Job> jobs = schedule.getJobSequence();
            
            if (jobs.size() < 2) continue;
            
            int bestMakespan = currentMakespan;
            int bestPos1 = -1, bestPos2 = -1;

            //on teste tous les swaps possibles
            for (int i = 0; i < jobs.size(); i++) {
                for (int j = i + 1; j < jobs.size(); j++) {
                    //ici swap
                    schedule.swapJobs(i, j);
                    solution.calculateMakespan();
                    int newMakespan = solution.getMakespan();
                    
                    if (newMakespan < bestMakespan) {
                        bestMakespan = newMakespan;
                        bestPos1 = i;
                        bestPos2 = j;
                    }
                    
                    //annuler le swap pour tester le suivant
                    schedule.swapJobs(i, j);
                }
            }

            //appliquer le meilleur swap trouvé
            if (bestPos1 != -1) {
                schedule.swapJobs(bestPos1, bestPos2);
                solution.calculateMakespan(); //FIX 
                improved = true;
                currentMakespan = bestMakespan;
            }
        }
        
        return improved;
    }
    
    /** ==========================================================================================
     * Opérateur 2 : Bottleneck External Insertion
     * 
     * Déplace un job d'une machine goulot vers une machine non-goulot
     * pour réduire la charge des machines goulot.
     * 
     * @param solution Solution à améliorer (modifiée sur place)
     * @return true si une amélioration a été trouvée
     */
    public boolean bottleneckExternalInsertion(Solution solution) {
        int currentMakespan = solution.getMakespan();
        List<Machine> bottlenecks = solution.getBottleneckMachines(); //init machines goulots
        
        boolean improved = false;
        
        //identifier les machines non-goulot aka celles qui ne sont pas dans la liste des goulots
        List<Machine> nonBottlenecks = new ArrayList<>();
        for (int m = 0; m < solution.getNumberOfMachines(); m++) {
            Machine machine = solution.getInstance().getMachine(m);
            if (!bottlenecks.contains(machine)) {
                nonBottlenecks.add(machine);
            }
        }
        
        if (nonBottlenecks.isEmpty()) return false; //si toutes les machines sont des goulots, on ne peut rien faire
        
        // for chaque machine goulot
        for (Machine bottleneck : bottlenecks) {
            Schedule bottleneckSchedule = solution.getSchedule(bottleneck); //on récup son planning
            List<Job> jobs = bottleneckSchedule.getJobSequence(); //et la séquence de jobs
            
            if (jobs.isEmpty()) continue; //si y'a pas de job, on passe au suivant

            //on prend une machine non-goulot random, on va essayer d'y insérer des jobs
            Random random = new Random();
            Machine targetMachine = nonBottlenecks.get(random.nextInt(nonBottlenecks.size()));
            Schedule targetSchedule = solution.getSchedule(targetMachine);
            
            int bestMakespan = currentMakespan; //on initialise le meilleur makespan au current (cf plus haut)
            Job bestJob = null;
            int bestPosition = -1;

            //on essaie de déplacer chaque job vers chaque position de la machine cible
            for (Job job : jobs) {
                for (int pos = 0; pos <= targetSchedule.getJobCount(); pos++) {
                    //rm job machine 
                    bottleneckSchedule.removeJob(job);
                    
                    //insert dans la machine cible
                    targetSchedule.addJob(job, pos);
                    solution.calculateMakespan();
                    int newMakespan = solution.getMakespan();
                    
                    //si amélioration ou même makespan avec réduction du completion time du goulot
                    if (newMakespan < bestMakespan || (newMakespan == currentMakespan && bottleneckSchedule.getCompletionTime() < currentMakespan)) {
                        bestMakespan = newMakespan;
                        bestJob = job;
                        bestPosition = pos;
                    }
                    
                    //annuler le mouvement si pas le meilleur
                    targetSchedule.removeJob(job);
                    bottleneckSchedule.addJob(job);
                }
            }
            
            // on apply le meilleur 
            if (bestJob != null) {
                bottleneckSchedule.removeJob(bestJob);
                targetSchedule.addJob(bestJob, bestPosition);
                solution.calculateMakespan();
                improved = true;
                currentMakespan = bestMakespan;
            }
        }
        
        return improved;
    }
    
    /* =========================================================================================
     * Opérateur 3 : Bottleneck External Swap
     * 
     * Échange un job d'une machine goulot avec un job d'une machine non-goulot
     * pour obtenir des jobs plus courts sur les machines goulot.
     */
    public boolean bottleneckExternalSwap(Solution solution) {
        int currentMakespan = solution.getMakespan();
        List<Machine> bottlenecks = solution.getBottleneckMachines();
        
        boolean improved = false;
        
        //identifier les machines non-goulot
        List<Machine> nonBottlenecks = new ArrayList<>();
        for (int m = 0; m < solution.getNumberOfMachines(); m++) {
            Machine machine = solution.getInstance().getMachine(m);
            if (!bottlenecks.contains(machine)) {
                nonBottlenecks.add(machine);
            }
        }
        
        if (nonBottlenecks.isEmpty()) return false;
        
        //for chaque machine goulot
        for (Machine bottleneck : bottlenecks) {
            Schedule bottleneckSchedule = solution.getSchedule(bottleneck);
            List<Job> bottleneckJobs = bottleneckSchedule.getJobSequence();
            
            if (bottleneckJobs.isEmpty()) continue;
            
            //same as before, on prend une machine non-goulot random
            Random random = new Random();
            Machine targetMachine = nonBottlenecks.get(random.nextInt(nonBottlenecks.size()));
            Schedule targetSchedule = solution.getSchedule(targetMachine);
            List<Job> targetJobs = targetSchedule.getJobSequence();
            
            if (targetJobs.isEmpty()) continue;
            
            int bestMakespan = currentMakespan;
            Job bestBottleneckJob = null;
            Job bestTargetJob = null;
            
            //on teste tous les swaps possibles entre les jobs des deux machines
            for (Job bottleneckJob : bottleneckJobs) {
                for (Job targetJob : targetJobs) {
                    // swap
                    int bottleneckPos = bottleneckSchedule.getJobSequence().indexOf(bottleneckJob);
                    int targetPos = targetSchedule.getJobSequence().indexOf(targetJob);
                    
                    bottleneckSchedule.removeJob(bottleneckJob); //FIX avec removeJOb 
                    targetSchedule.removeJob(targetJob);
                    
                    bottleneckSchedule.addJob(targetJob, bottleneckPos);
                    targetSchedule.addJob(bottleneckJob, targetPos);
                    
                    solution.calculateMakespan();
                    int newMakespan = solution.getMakespan();
                    
                    //si amélioration ou même makespan avec réduction du completion time du goulot
                    if (newMakespan < bestMakespan || (newMakespan == currentMakespan && Math.max(bottleneckSchedule.getCompletionTime(), targetSchedule.getCompletionTime()) < currentMakespan)) {
                        bestMakespan = newMakespan;
                        bestBottleneckJob = bottleneckJob;
                        bestTargetJob = targetJob;
                    }
                    
                    //annuler le swap si pas le meilleur
                    bottleneckSchedule.removeJob(targetJob);
                    targetSchedule.removeJob(bottleneckJob);
                    
                    bottleneckSchedule.addJob(bottleneckJob, bottleneckPos);
                    targetSchedule.addJob(targetJob, targetPos);
                }
            }
            
            //on applique le meilleur swap trouvé 
            if (bestBottleneckJob != null) {
                int bottleneckPos = bottleneckSchedule.getJobSequence().indexOf(bestBottleneckJob);
                int targetPos = targetSchedule.getJobSequence().indexOf(bestTargetJob);
                
                bottleneckSchedule.removeJob(bestBottleneckJob);
                targetSchedule.removeJob(bestTargetJob);
                
                bottleneckSchedule.addJob(bestTargetJob, bottleneckPos);
                targetSchedule.addJob(bestBottleneckJob, targetPos);
                
                solution.calculateMakespan();
                improved = true;
                currentMakespan = bestMakespan;
            }
        }
        
        return improved;
    }
    
    /* =========================================================================================
     * Opérateur 4 : Balancing
     * 
     * Équilibre la charge entre machines en déplaçant le dernier job
     * d'une machine goulot vers des machines non-goulot.
     */
    public boolean balancing(Solution solution) {
        boolean improved = false;
        boolean madeChange;
        
        do {
            madeChange = false;
            int currentMakespan = solution.getMakespan();
            List<Machine> bottlenecks = solution.getBottleneckMachines();
            
            //chaque machine goulot
            for (Machine bottleneck : bottlenecks) {
                Schedule bottleneckSchedule = solution.getSchedule(bottleneck);
                List<Job> jobs = bottleneckSchedule.getJobSequence();
                
                if (jobs.isEmpty()) continue; // aucun job à déplacer, on passe au suivant

                //on prend le dernier job
                Job lastJob = jobs.get(jobs.size() - 1);
                
                int bestMakespan = currentMakespan;
                Machine bestMachine = null;
                int bestPosition = -1;

                //essayer de l'insérer dans chaque machine non-goulot
                for (int m = 0; m < solution.getNumberOfMachines(); m++) {
                    Machine machine = solution.getInstance().getMachine(m);
                    if (bottlenecks.contains(machine)) continue;
                    
                    Schedule targetSchedule = solution.getSchedule(machine);
                    
                    //chaque pos testée
                    for (int pos = 0; pos <= targetSchedule.getJobCount(); pos++) {
                        bottleneckSchedule.removeJob(lastJob);
                        targetSchedule.addJob(lastJob, pos);
                        
                        solution.calculateMakespan();
                        int newMakespan = solution.getMakespan();
                        
                        if (newMakespan < bestMakespan) {
                            bestMakespan = newMakespan;
                            bestMachine = machine;
                            bestPosition = pos;
                        }
                        
                        //delete if not best
                        targetSchedule.removeJob(lastJob);
                        bottleneckSchedule.addJob(lastJob);
                    }
                }
                
                //meilleur mouvement
                if (bestMachine != null) {
                    bottleneckSchedule.removeJob(lastJob);
                    solution.getSchedule(bestMachine).addJob(lastJob, bestPosition);
                    solution.calculateMakespan();
                    madeChange = true;
                    improved = true;
                    break; //FIX important, permet de recommencer avec les nouveaux goulots //TODO
                }
            }
        } while (madeChange);
        
        return improved;
    }
    
    /**
     * Opérateur 5 : Inter Machine Insertion
     * 
     * Trouve de meilleures positions pour les jobs en les déplaçant entre machines.
     * Applique l'équation (1) du papier : Ck - Ck_new > Ch_new - Ch AND Cmax_new <= Cmax
     */
    public boolean interMachineInsertion(Solution solution) {
        boolean improved = false;
        int moveCount = 0;
        int maxMoves = solution.getNumberOfMachines() * solution.getNumberOfMachines();
        
        while (moveCount < maxMoves) {
            boolean madeMove = false;

            //Pour chaque paire de machines (k, h)
            for (int k = 0; k < solution.getNumberOfMachines(); k++) {
                for (int h = 0; h < solution.getNumberOfMachines(); h++) {
                    if (k == h) continue;
                    
                    Schedule scheduleK = solution.getSchedule(k);
                    Schedule scheduleH = solution.getSchedule(h);
                    
                    if (scheduleK.getJobCount() == 0) continue;
                    
                    int currentCk = scheduleK.getCompletionTime();
                    int currentCh = scheduleH.getCompletionTime();
                    int currentMakespan = solution.getMakespan();
                    
                    Job bestJob = null;
                    int bestPosition = -1;
                    double bestGain = 0;

                    //on essaie de déplacer chaque job de k vers h
                    for (Job job : scheduleK.getJobSequence()) {
                        for (int pos = 0; pos <= scheduleH.getJobCount(); pos++) {
                            //mouvement
                            scheduleK.removeJob(job);
                            scheduleH.addJob(job, pos);
                            solution.calculateMakespan();
                            
                            int newCk = scheduleK.getCompletionTime();
                            int newCh = scheduleH.getCompletionTime();
                            int newMakespan = solution.getMakespan();
                            
                            // équation (1) du papier //TODO vérifier si c'est bien ça
                            double gainK = currentCk - newCk;
                            double costH = newCh - currentCh;
                            
                            if (gainK > costH && newMakespan <= currentMakespan) {
                                double netGain = gainK - costH;
                                if (netGain > bestGain) {
                                    bestGain = netGain;
                                    bestJob = job;
                                    bestPosition = pos;
                                }
                            }
                            
                            //annuler le mouvement
                            scheduleH.removeJob(job);
                            scheduleK.addJob(job);
                        }
                    }
                    
                    //meilleur mouvement trouvé
                    if (bestJob != null) {
                        scheduleK.removeJob(bestJob);
                        scheduleH.addJob(bestJob, bestPosition);
                        solution.calculateMakespan();
                        madeMove = true;
                        improved = true;
                        moveCount++;
                        break; //same as before, on recommence avec les nouvelles données FIX 
                    }
                }
                if (madeMove) break;
            }

            if (!madeMove) break; //pas de mouvement bénéfique trouvé => on arrête
        }
        
        return improved;
    }
}