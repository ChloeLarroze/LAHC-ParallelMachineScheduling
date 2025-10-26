package utils;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import solution.Solution;
import solution.Schedule;
import domain.Job;
import domain.Machine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GanttFX {
    
    //cts 4 drawing and layout
    private static final int ROW_HEIGHT = 60;
    private static final int LEFT_MARGIN = 120;
    private static final int TOP_MARGIN = 60;
    private static final int TIME_SCALE = 25; // pixels per time unit
    private static final int MIN_CANVAS_WIDTH = 900;
    
    //color palette for jobs thks to https://stackoverflow.com/a/4382138 and copilot :)
    private static final Color[] JOB_COLORS = {
        Color.rgb(100, 149, 237), // Cornflower blue
        Color.rgb(255, 127, 80),  // Coral
        Color.rgb(144, 238, 144), // Light green
        Color.rgb(255, 182, 193), // Light pink
        Color.rgb(221, 160, 221), // Plum
        Color.rgb(255, 218, 185), // Peach
        Color.rgb(176, 224, 230), // Powder blue
        Color.rgb(240, 230, 140), // Khaki
        Color.rgb(152, 251, 152), // Pale green
        Color.rgb(255, 160, 122)  // Light salmon
    };
    
    // sijk - Setup time color (gray)
    private static final Color SETUP_COLOR = Color.rgb(200, 200, 200);
    private static final Color SETUP_BORDER = Color.rgb(100, 100, 100);
    
    public static void showGantt(Solution solution, String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        
        BorderPane root = new BorderPane();
        
        //info label at top
        Label infoLabel = new Label(String.format(
            "Makespan: %d | Jobs: %d | Machines: %d",
            solution.getMakespan(),
            solution.getInstance().getNumberOfJobs(),
            solution.getInstance().getNumberOfMachines()
        ));
        infoLabel.setStyle("-fx-padding: 10; -fx-font-size: 14px; -fx-font-weight: bold;");
        root.setTop(infoLabel);
        
        //canvas https://stackoverflow.com/questions/1255453/scrollable-flow-panel/1255509#1255509
        Canvas canvas = createGanttCanvas(solution);
        Pane canvasPane = new Pane(canvas);
        ScrollPane scrollPane = new ScrollPane(canvasPane);
        scrollPane.setFitToHeight(true);
        
        root.setCenter(scrollPane);
        
        Scene scene = new Scene(root, 1000, 650);
        stage.setScene(scene);
        stage.show();
    }
    
    private static Canvas createGanttCanvas(Solution solution) {
        int numMachines = solution.getNumberOfMachines();
        double makespan = solution.getMakespan();
        
        int canvasWidth = Math.max(MIN_CANVAS_WIDTH, 
            LEFT_MARGIN + (int)(makespan * TIME_SCALE) + 100);
        int canvasHeight = TOP_MARGIN + (numMachines * ROW_HEIGHT) + 80;
        
        Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        //background
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
        
        //grid and machine labels
        drawGrid(gc, numMachines, makespan, canvasWidth);
        
        // Assign colors to jobs
        Map<Integer, Color> jobColors = assignJobColors(solution);
        
        // Draw jobs and setups on machines
        drawJobsAndSetups(gc, solution, jobColors);
        
        // Draw release times
        drawReleaseTimes(gc, solution, jobColors);
        
        // Draw legend
        drawLegend(gc, solution, jobColors, canvasWidth);
        
        return canvas;
    }
    
    private static void drawGrid(GraphicsContext gc, int numMachines, 
                                  double makespan, int canvasWidth) {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        
        //horizontal lines for each machine
        for (int i = 0; i <= numMachines; i++) {
            int y = TOP_MARGIN + i * ROW_HEIGHT;
            gc.strokeLine(LEFT_MARGIN, y, canvasWidth - 20, y);
        }
        
        // machine labels
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 14));
        for (int i = 0; i < numMachines; i++) {
            int y = TOP_MARGIN + i * ROW_HEIGHT + ROW_HEIGHT / 2 + 5;
            gc.fillText("Machine " + (i + 1), 10, y);
        }
        
        // Draw time axis
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(LEFT_MARGIN, TOP_MARGIN + numMachines * ROW_HEIGHT,
                     canvasWidth - 20, TOP_MARGIN + numMachines * ROW_HEIGHT);
        
        // Draw time markers
        gc.setFont(new Font("Arial", 10));
        int timeStep = Math.max(1, (int)(makespan / 10));
        if (timeStep == 0) timeStep = 1;
        for (int t = 0; t <= makespan; t += timeStep) {
            int x = LEFT_MARGIN + (int)(t * TIME_SCALE);
            int y = TOP_MARGIN + numMachines * ROW_HEIGHT;
            gc.strokeLine(x, y, x, y + 5);
            gc.fillText(String.valueOf(t), x - 5, y + 20);
        }
    }
    
    private static Map<Integer, Color> assignJobColors(Solution solution) {
        Map<Integer, Color> jobColors = new HashMap<>();
        int colorIndex = 0;
        
        for (int i = 0; i < solution.getNumberOfMachines(); i++) {
            Schedule schedule = solution.getSchedule(i);
            for (Job job : schedule.getJobSequence()) {
                if (!jobColors.containsKey(job.getId())) {
                    jobColors.put(job.getId(), JOB_COLORS[colorIndex % JOB_COLORS.length]);
                    colorIndex++;
                }
            }
        }
        
        return jobColors;
    }
    
    private static void drawJobsAndSetups(GraphicsContext gc, Solution solution, 
                                           Map<Integer, Color> jobColors) {
        for (int machineIdx = 0; machineIdx < solution.getNumberOfMachines(); machineIdx++) {
            Schedule schedule = solution.getSchedule(machineIdx);
            Machine machine = schedule.getMachine();
            List<Job> jobSequence = schedule.getJobSequence();
            
            if (jobSequence.isEmpty()) continue;
            
            int currentTime = 0;
            Job previousJob = null;
            
            for (Job job : jobSequence) {
                // Get setup and processing times
                int setupTime = solution.getInstance().getSetupTime(previousJob, job, machine);
                int processingTime = solution.getInstance().getProcessingTime(job, machine);
                
                // Setup cannot start before release date
                int setupStartTime = Math.max(currentTime, job.getReleaseDate());
                
                // Draw idle time if there's a gap (waiting for release date)
                if (setupStartTime > currentTime) {
                    drawIdleTime(gc, currentTime, setupStartTime - currentTime, machineIdx);
                }
                
                // Draw setup time block if non-zero
                if (setupTime > 0) {
                    drawSetupBlock(gc, setupStartTime, setupTime, machineIdx, previousJob, job);
                }
                
                // Processing starts after setup
                int processingStartTime = setupStartTime + setupTime;
                
                // Draw job processing block
                drawJobBlock(gc, processingStartTime, processingTime, machineIdx, job, jobColors);
                
                // Update current time
                currentTime = processingStartTime + processingTime;
                previousJob = job;
            }
        }
    }
    
    private static void drawIdleTime(GraphicsContext gc, int startTime, int duration, int machineIdx) {
        int x = LEFT_MARGIN + (int)(startTime * TIME_SCALE);
        int y = TOP_MARGIN + machineIdx * ROW_HEIGHT + 5;
        int width = (int)(duration * TIME_SCALE);
        int height = ROW_HEIGHT - 10;
        
        // Draw white/empty block
        gc.setFill(Color.WHITE);
        gc.fillRect(x, y, width, height);
        
        // Draw dashed border to indicate idle time
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        gc.setLineDashes(5, 5);
        gc.strokeRect(x, y, width, height);
        gc.setLineDashes(null); // Reset to solid line
        
        // Label if there's space
        if (width > 30) {
            gc.setFill(Color.GRAY);
            gc.setFont(new Font("Arial Italic", 10));
            //gc.fillText("idle", x + width/2 - 10, y + height/2 + 4); FIX too crowded
        }
    }
    
    private static void drawSetupBlock(GraphicsContext gc, int startTime, int setupTime, 
                                        int machineIdx, Job prevJob, Job nextJob) {
        int x = LEFT_MARGIN + (int)(startTime * TIME_SCALE);
        int y = TOP_MARGIN + machineIdx * ROW_HEIGHT + 5;
        int width = (int)(setupTime * TIME_SCALE);
        int height = ROW_HEIGHT - 10;
        
        // Draw setup rectangle with diagonal stripes pattern
        gc.setFill(SETUP_COLOR);
        gc.fillRect(x, y, width, height);
        
        // Draw diagonal stripes
        gc.setStroke(Color.rgb(150, 150, 150));
        gc.setLineWidth(1);
        for (int i = 0; i < width + height; i += 8) {
            gc.strokeLine(x + i, y, x + i - height, y + height);
        }
        
        // Draw border
        gc.setStroke(SETUP_BORDER);
        gc.setLineWidth(1);
        gc.strokeRect(x, y, width, height);
        
        // Draw label if there's space
        if (width > 30) {
            gc.setFill(Color.BLACK);
            gc.setFont(new Font("Arial", 10));
            String label = prevJob == null ? "S" + nextJob.getId() : "S" + prevJob.getId() + "→" + nextJob.getId();
            gc.fillText(label, x + 3, y + height / 2);
            gc.fillText("(" + setupTime + ")", x + 3, y + height / 2 + 12);
        }
    }
    
    private static void drawJobBlock(GraphicsContext gc, int startTime, int processingTime,
                                      int machineIdx, Job job, Map<Integer, Color> jobColors) {
        int x = LEFT_MARGIN + (int)(startTime * TIME_SCALE);
        int y = TOP_MARGIN + machineIdx * ROW_HEIGHT + 5;
        int width = (int)(processingTime * TIME_SCALE);
        int height = ROW_HEIGHT - 10;
        
        // Draw job rectangle
        Color jobColor = jobColors.get(job.getId());
        gc.setFill(jobColor);
        gc.fillRect(x, y, width, height);
        
        // Draw border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, width, height);
        
        // Draw job label
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial Bold", 13));
        String label = "J" + job.getId();
        gc.fillText(label, x + 5, y + height / 2 + 2);
        
        // Draw processing time if there's space
        if (width > 35) {
            gc.setFont(new Font("Arial", 11));
            gc.fillText("p=" + processingTime, x + 5, y + height / 2 + 16);
        }
        
        // Draw start time below the block
        gc.setFont(new Font("Arial", 9));
        gc.setFill(Color.DARKBLUE);
        gc.fillText(String.valueOf(startTime), x + 2, y + height + 12);
    }
    
    private static void drawReleaseTimes(GraphicsContext gc, Solution solution,
                                          Map<Integer, Color> jobColors) {
        // Collect all jobs and their release times
        Map<Job, Integer> jobReleases = new HashMap<>();
        
        for (int machineIdx = 0; machineIdx < solution.getNumberOfMachines(); machineIdx++) {
            Schedule schedule = solution.getSchedule(machineIdx);
            for (Job job : schedule.getJobSequence()) {
                if (!jobReleases.containsKey(job)) {
                    jobReleases.put(job, job.getReleaseDate());
                }
            }
        }
        
        // Draw release time markers for each job
        for (Map.Entry<Job, Integer> entry : jobReleases.entrySet()) {
            Job job = entry.getKey();
            int releaseTime = entry.getValue();
            
            // Skip if release time is 0 (too crowded at start)
            if (releaseTime == 0) continue;
            
            Color jobColor = jobColors.get(job.getId());
            int x = LEFT_MARGIN + (int)(releaseTime * TIME_SCALE);
            
            // Draw for each machine row where this job appears
            for (int machineIdx = 0; machineIdx < solution.getNumberOfMachines(); machineIdx++) {
                Schedule schedule = solution.getSchedule(machineIdx);
                if (schedule.getJobSequence().contains(job)) {
                    int y = TOP_MARGIN + machineIdx * ROW_HEIGHT;
                    
                    // Draw arrow pointing down
                    gc.setStroke(jobColor);
                    gc.setLineWidth(2);
                    gc.strokeLine(x, y - 15, x, y - 2);
                    
                    // Arrow head
                    gc.strokeLine(x, y - 2, x - 3, y - 6);
                    gc.strokeLine(x, y - 2, x + 3, y - 6);
                    
                    // Draw label
                    gc.setFill(jobColor);
                    gc.setFont(new Font("Arial Bold", 10));
                    gc.fillText("r" + job.getId() + "=" + releaseTime, x - 15, y - 18);
                }
            }
        }
    }
    
    private static void drawLegend(GraphicsContext gc, Solution solution,
                                    Map<Integer, Color> jobColors, int canvasWidth) {
        gc.setFont(new Font("Arial Bold", 12));
        gc.setFill(Color.BLACK);
        
        int legendX = canvasWidth - 180;
        int legendY = TOP_MARGIN;
        
        gc.fillText("Légende:", legendX, legendY);
        legendY += 25;
        
        // Setup time entry
        gc.setFill(SETUP_COLOR);
        gc.fillRect(legendX, legendY, 20, 15);
        gc.setStroke(SETUP_BORDER);
        gc.strokeRect(legendX, legendY, 20, 15);
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 11));
        gc.fillText("Temps de setup", legendX + 25, legendY + 12);
        legendY += 25;
        
        // Release time entry
        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(2);
        gc.strokeLine(legendX + 5, legendY, legendX + 5, legendY + 12);
        gc.strokeLine(legendX + 5, legendY + 12, legendX + 2, legendY + 8);
        gc.strokeLine(legendX + 5, legendY + 12, legendX + 8, legendY + 8);
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 11));
        gc.fillText("Release time (rⱼ)", legendX + 25, legendY + 12);
        legendY += 25;
        
        // Job entries
        gc.setFont(new Font("Arial Bold", 11));
        gc.fillText("Jobs:", legendX, legendY);
        legendY += 20;
        
        int count = 0;
        for (Map.Entry<Integer, Color> entry : jobColors.entrySet()) {
            if (count >= 8) break; // Limit legend entries
            
            gc.setFill(entry.getValue());
            gc.fillRect(legendX, legendY, 20, 15);
            
            gc.setStroke(Color.BLACK);
            gc.strokeRect(legendX, legendY, 20, 15);
            
            gc.setFill(Color.BLACK);
            gc.setFont(new Font("Arial", 11));
            gc.fillText("Job " + entry.getKey(), legendX + 25, legendY + 12);
            
            legendY += 22;
            count++;
        }
    }
}