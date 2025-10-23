// File: gui/Controller.java
package gui;

import domain.*;
import solution.*;
import algo.heuristic.*;
import algo.metaheuristic.*;
import utils.*;

// import  gui.visualizers.GanttChart;
// import  gui.visualizers.LoadHeatmap;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;

// Contrôleur principal pour l'interface JavaFX
//gère les interactions utilisateur et la logique de l'application
public class Controller {
    
    // === FXML Components ===
    @FXML private TextField jobsField;
    @FXML private TextField machinesField;
    @FXML private Spinner<Integer> lhSpinner;
    @FXML private Spinner<Integer> iterationsSpinner;
    @FXML private Button loadFileButton;
    @FXML private Button generateRandomButton;
    @FXML private Button runLAHCButton;
    @FXML private Button stopButton;
    
    @FXML private TextArea logArea;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    
    @FXML private Label makespanLabel;
    @FXML private Label executionTimeLabel;
    @FXML private Label improvementLabel;
    
    @FXML private Pane ganttPane;
    @FXML private Pane heatmapPane;
    
    @FXML private TabPane resultTabPane;
    
    // === State ===
    private Instance currentInstance;
    private Solution currentSolution;
    private Task<Solution> currentTask;
    
    /**
     * Initialisation du contrôleur (appelé automatiquement par JavaFX).
     */
    @FXML
    public void initialize() {
        log("Application initialisée.");
        
        // Configurer les spinners
        lhSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 100, 30, 5));
        iterationsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(100, 10000, 1000, 100));
        
        // Désactiver les boutons jusqu'à ce qu'une instance soit chargée
        runLAHCButton.setDisable(true);
        stopButton.setDisable(true);
        
        // Charger l'instance exemple du papier par défaut
        InstanceReader.createPaperInstance();
    }
    
    // === Event Handlers ===
    
    @FXML
    private void handleLoadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Charger une instance");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        
        File file = fileChooser.showOpenDialog(loadFileButton.getScene().getWindow());
        
        if (file != null) {
            try {
                currentInstance = InstanceReader.readFromFile(file.getAbsolutePath());
                updateInstanceInfo();
                log("Instance chargée: " + file.getName());
                runLAHCButton.setDisable(false);
            } catch (Exception e) {
                showError("Erreur de chargement", "Impossible de charger le fichier: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleGenerateRandom() {
        try {
            int numJobs = Integer.parseInt(jobsField.getText());
            int numMachines = Integer.parseInt(machinesField.getText());
            
            if (numJobs < 1 || numMachines < 1) {
                showError("Paramètres invalides", "Le nombre de jobs et machines doit être >= 1");
                return;
            }
            
            log("Génération d'une instance aléatoire: " + numJobs + " jobs, " + numMachines + " machines");
            
            currentInstance = InstanceReader.createRandomInstance(numJobs, numMachines, 100, 20, 0.5);
            updateInstanceInfo();
            runLAHCButton.setDisable(false);
            
        } catch (NumberFormatException e) {
            showError("Paramètres invalides", "Veuillez entrer des nombres valides");
        }
    }
    
    @FXML
    private void handleRunLAHC() {
        if (currentInstance == null) {
            showError("Aucune instance", "Veuillez charger ou générer une instance d'abord");
            return;
        }
        
        // Désactiver les contrôles
        runLAHCButton.setDisable(true);
        stopButton.setDisable(false);
        loadFileButton.setDisable(true);
        generateRandomButton.setDisable(true);
        
        // Créer et lancer la tâche en arrière-plan
        currentTask = createLAHCTask();
        
        // Lier la progression
        progressBar.progressProperty().bind(currentTask.progressProperty());
        
        // Gérer la fin
        currentTask.setOnSucceeded(event -> {
            currentSolution = currentTask.getValue();
            displayResults();
            resetControls();
            log("LAHC terminé avec succès.");
        });
        
        currentTask.setOnFailed(event -> {
            showError("Erreur", "Erreur durant l'exécution: " + currentTask.getException().getMessage());
            resetControls();
        });
        
        currentTask.setOnCancelled(event -> {
            log("Exécution annulée par l'utilisateur.");
            resetControls();
        });
        
        // Lancer dans un thread séparé
        Thread thread = new Thread(currentTask);
        thread.setDaemon(true);
        thread.start();
    }
    
    @FXML
    private void handleStop() {
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel();
            log("Arrêt demandé...");
        }
    }
    
    @FXML
    private void handleLoadPaperExample() {
        InstanceReader.createPaperInstance();
    }
    
    // === Business Logic ===
    
    /**
     * Crée une tâche JavaFX pour exécuter LAHC en arrière-plan.
     */
    private Task<Solution> createLAHCTask() {
        return new Task<Solution>() {
            @Override
            protected Solution call() throws Exception {
                updateMessage("Initialisation...");
                updateProgress(0, 100);
                
                // Créer LAHC
                BIBAHeuristic biba = new BIBAHeuristic();
                LAHCMetaheuristic lahc = new LAHCMetaheuristic(biba, lhSpinner.getValue());
                lahc.setMaxIterations(iterationsSpinner.getValue());
                
                logAsync("Génération de la solution initiale avec BIBA...");
                
                // Résoudre
                Solution solution = lahc.solve(currentInstance);
                
                updateProgress(100, 100);
                updateMessage("Terminé");
                
                return solution;
            }
        };
    }
    
    // === UI Updates ===
    
    /**
     * Met à jour l'affichage des informations de l'instance.
     */
    private void updateInstanceInfo() {
        if (currentInstance != null) {
            jobsField.setText(String.valueOf(currentInstance.getNumberOfJobs()));
            machinesField.setText(String.valueOf(currentInstance.getNumberOfMachines()));
            statusLabel.setText("Instance chargée: " + currentInstance.getNumberOfJobs() + 
                              " jobs, " + currentInstance.getNumberOfMachines() + " machines");
        }
    }
    
    /**
     * Affiche les résultats de LAHC.
     */
    private void displayResults() {
        if (currentSolution == null) return;
        
        // Statistiques
        makespanLabel.setText("Makespan: " + currentSolution.getMakespan());
        
        // // Gantt chart
        // GanttChart gantt = new GanttChart(currentSolution);
        // ganttPane.getChildren().clear();
        // ganttPane.getChildren().add(gantt.getChart());
        
        // // Heatmap
        // LoadHeatmap heatmap = new LoadHeatmap(currentSolution);
        // heatmapPane.getChildren().clear();
        // heatmapPane.getChildren().add(heatmap.getChart());
        
        // Passer à l'onglet résultats
        resultTabPane.getSelectionModel().select(1);
    }
    
    /**
     * Réactive les contrôles après exécution.
     */
    private void resetControls() {
        runLAHCButton.setDisable(false);
        stopButton.setDisable(true);
        loadFileButton.setDisable(false);
        generateRandomButton.setDisable(false);
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
    }
    
    /**
     * Ajoute un message au log.
     */
    private void log(String message) {
        logArea.appendText("[" + java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + message + "\n");
    }
    
    /**
     * Ajoute un message au log depuis un thread séparé.
     */
    private void logAsync(String message) {
        Platform.runLater(() -> log(message));
    }
    
    /**
     * Affiche une erreur.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}