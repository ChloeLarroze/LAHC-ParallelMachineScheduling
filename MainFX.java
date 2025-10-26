import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import algo.metaheuristic.LAHCMetaheuristic;
import algo.heuristic.BIBAHeuristic;
import domain.Instance;
import utils.InstanceReader;
import utils.GanttFX;
import solution.Solution;

public class MainFX extends Application {

    private TextArea outputArea;
    private Button ganttButton;
    private Solution currentSolution;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Parallel Machine Scheduling - LAHC");

        //choix type entrée
        ComboBox<String> inputChoice = new ComboBox<>();
        inputChoice.getItems().addAll("Instance aléatoire", "Depuis un fichier", "Entrée manuelle");
        inputChoice.setValue("Instance aléatoire"); //valeur par défaut

        Button runButton = new Button("Lancer la résolution");
        ganttButton = new Button("Afficher le Gantt");
        ganttButton.setDisable(true); //tant qu'il n'y a pas de solution on peut pas generer diagramme 
        
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(300);

        runButton.setOnAction(e -> runAlgorithm(inputChoice.getValue()));
        ganttButton.setOnAction(e -> showGantt());

        // Layout pour les boutons
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(runButton, ganttButton);

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 15;");
        layout.getChildren().addAll(
            new Label("Type d'entrée :"),
            inputChoice,
            buttonBox,
            new Label("Résultats :"),
            outputArea
        );

        Scene scene = new Scene(layout, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    /// === FONCTIONS ===
    ///algorithme selon le type d'entrée choisi
    private void runAlgorithm(String inputType) {
        try {
            Instance instance;

            switch (inputType) {
                case "Depuis un fichier":
                    instance = InstanceReader.readFromFile("resources/Instance.txt"); //TODO : file chooser maybe ? 
                    break;
                case "Entrée manuelle":
                    outputArea.appendText("Mode manuel non encore implémenté.\n");
                    return;
                default: //random instance
                    int randomJobNumber = 5 + (int)(Math.random() * 6); // 5 à 10 jobs
                    int randomMachineNumber = 2 + (int)(Math.random() * 3); // 2 à 4 machines
                    instance = InstanceReader.createRandomInstance(randomJobNumber, randomMachineNumber, 10, 5, 0.5);
            }

            outputArea.appendText("=================================\n");
            outputArea.appendText("Nouvelle instance :\n");
            outputArea.appendText("Jobs: " + instance.getNumberOfJobs() + "\n");
            outputArea.appendText("Machines: " + instance.getNumberOfMachines() + "\n");
            outputArea.appendText("Résolution en cours...\n");

            //LAHC exec
            BIBAHeuristic heuristic = new BIBAHeuristic();
            LAHCMetaheuristic lahc = new LAHCMetaheuristic(heuristic);
            currentSolution = lahc.solve(instance);

            outputArea.appendText("\nRésolution terminée :\n");
            outputArea.appendText("Makespan final : " + currentSolution.getMakespan() + "\n");
            outputArea.appendText(currentSolution.toString() + "\n");
            
            //Gantt button enable here 
            ganttButton.setDisable(false);

        } catch (Exception ex) {
            outputArea.appendText("Erreur : " + ex.getMessage() + "\n");
            ex.printStackTrace();
        }
    }

    private void showGantt() {
        if (currentSolution != null) {
            GanttFX.showGantt(currentSolution, "Diagramme de Gantt - Solution");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}