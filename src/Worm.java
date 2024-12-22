import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Random;

public class Worm extends Application {

    // Configurable constants
    private static final int GRID_WIDTH = 200;  // Nombre de colonnes
    private static final int GRID_HEIGHT = 10; // Nombre de lignes
    private int CELL_SIZE = 20;   // Taille d'une cellule en pixels
    private static final double DEFAULT_SEED_DENSITY = 0.3;
    private long DEFAULT_UPTDATE_INTERVAL = 100; // in ms

    // Grid and application states
    private int[][] grid = new int[GRID_WIDTH][GRID_HEIGHT]; // État de la grille
    private boolean isPaused = false; // Indicateur pour savoir si le jeu est en pause
    private Canvas canvas;
    private Label fpsLabel;
    private double seedDensity = DEFAULT_SEED_DENSITY;

    // Performace tracking
    private int frameCount;
    private long lastFpsUpdateTime;

    /* ------------ Application ------------ */
    @Override
    public void start(Stage primaryStage) {

        // Create Main application Layout
        Pane root = createAppLayout();
        
        // Randomize Initial Grid
        initializeRandomGrid(DEFAULT_SEED_DENSITY);

        appLoop();

        // Scene setup
        Scene scene = new Scene(root, 800, 800);

        primaryStage.setTitle("Worm cellular automata");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    /* ------------ UI COMPONENTS ------------ */
    private VBox createControlPanel(GraphicsContext gc) {
        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(e -> {
            isPaused = !isPaused;
            pauseButton.setText(isPaused ?"Resume" : "Pause");
        }); // Pause ou reprise du jeu

        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> {
            initializeRandomGrid(seedDensity); // Réinitialise la grille
            drawGrid(gc);             // Redessine la grille
        });

        Label seedDensityLabel = new Label("Seed Density");
        seedDensityLabel.setTextFill(Color.WHITE);

        Label sliderLabel = new Label(String.format("%.2f", DEFAULT_SEED_DENSITY));
        sliderLabel.setTextFill(Color.WHITE);

        Slider seedDensitySlider = new Slider(0, 1, DEFAULT_SEED_DENSITY);
        seedDensitySlider.setShowTickLabels(true);
        seedDensitySlider.valueProperty().addListener((_, _, newValue) -> {
            double value = newValue.doubleValue();
            seedDensity = value;
            sliderLabel.setText(String.format("%.2f", value));
        });

        // FPS Label
        fpsLabel = new Label("FPS: 0");
        fpsLabel.setTextFill(Color.WHITE);

        return new VBox(10, fpsLabel, pauseButton, resetButton, seedDensityLabel, sliderLabel, seedDensitySlider);
    }

    private Pane createAppLayout() {
        // Create Canvas
        canvas = new Canvas(
            GRID_WIDTH * CELL_SIZE, 
            GRID_HEIGHT * CELL_SIZE
        );
        

        VBox controlPanel = createControlPanel(canvas.getGraphicsContext2D());

        // Position du menu dans le coin supérieur droit
        controlPanel.setLayoutX(GRID_WIDTH * CELL_SIZE - 160); // Décalage pour positionner le menu à droite
        controlPanel.setLayoutY(10); // Décalage du haut

        controlPanel.setAlignment(Pos.TOP_CENTER);
        controlPanel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-padding: 10; -fx-border-color: black; -fx-border-width: 1;");
        controlPanel.setPrefWidth(150); // Largeur fixe
        controlPanel.setPrefHeight(100); // Hauteur fixe

        Pane root = new Pane(canvas, controlPanel);

        root.widthProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setLayoutX((newVal.doubleValue() - canvas.getWidth()) / 2);
            controlPanel.setLayoutX(newVal.doubleValue() - controlPanel.getPrefWidth() - 10); // Ajuste la position du menu
        });
        
        root.heightProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setLayoutY((newVal.doubleValue() - canvas.getHeight()) / 2);
            controlPanel.setLayoutY(10); // Le menu reste toujours en haut
        });

        // Handle Mouse Draw with canva
        canvas.setOnMousePressed(this::handleMouseDraw);
        canvas.setOnMouseDragged(this::handleMouseDraw);
        
        // Canva scroll
        canvas.setOnScroll(event -> {
            double deltaY = event.getDeltaY();
            if (deltaY > 0) {
                CELL_SIZE = Math.min(CELL_SIZE + 2, 50);
            } else {
                CELL_SIZE = Math.max(CELL_SIZE - 2, 5);
            }
            canvas.setWidth(GRID_WIDTH * CELL_SIZE);
            canvas.setHeight(GRID_HEIGHT * CELL_SIZE);
        
            // Recentre le Canvas
            canvas.setLayoutX((root.getWidth() - canvas.getWidth()) / 2);
            canvas.setLayoutY((root.getHeight() - canvas.getHeight()) / 2);
        
            drawGrid(canvas.getGraphicsContext2D());
        });

        return root;
    }

    private void trackFPS(long now) {
        frameCount++;
        
        // Update FPS every second
        if (now - lastFpsUpdateTime >= 1_000_000_000) {
            double fps = frameCount * 1_000_000_000.0 / (now - lastFpsUpdateTime);
            fpsLabel.setText(String.format("FPS: %.1f", fps));
            
            // Reset counters
            frameCount = 0;
            lastFpsUpdateTime = now;
        }
    }

    /* ------------ APPLICATION LOOP ------------ */
    private void appLoop() {
        AnimationTimer timer = new AnimationTimer() {
            long lastUpdate = 0;

            @Override
            public void handle(long now) {

                trackFPS(now);

                if (isPaused) return; // Ne fait rien si le jeu est en pause

                long elapsedTime = now - lastUpdate;
                if (elapsedTime >= DEFAULT_UPTDATE_INTERVAL * 1_000_000) {
                    updateGrid();
                    drawGrid(canvas.getGraphicsContext2D());
                    lastUpdate = now;
                }
            }
        };
        timer.start();
    }

    /* ------------ GRID FUNCTIONS ------------ */
    private void initializeRandomGrid(double rate) {
        Random random = new Random();
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                grid[x][y] = random.nextDouble() >= rate ? 0 : 1;
            }
        }
    }

    private void updateGrid() {
        int[][] newGrid = new int[GRID_WIDTH][GRID_HEIGHT];
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                int currentTile = grid[x][y];
                int neighbors = countNeighbors(x, y, 1);

                switch(currentTile) {
                    // Air
                    case 0:
                        newGrid[x][y] = neighbors == 1 ? 1 : 0;
                        break;
                    // Food
                    case 2:
                        if(checkGridBounds(x - 1, y)) {
                            newGrid[x - 1][y] = neighbors == 1 ? 1 : grid[x - 1][y];
                            newGrid[x][y] = neighbors == 1 ? 1 : 2;
                        }
                        break;
                    // 
                    case 1:
                        newGrid[x][y] = neighbors == 0 ? 0 : 1;
                        break;
                    // Anything else
                    default:
                        newGrid[x][y] = grid[x][y];
                        break;
                }
                
            }
        }
        grid = newGrid;
    }

    private int countNeighbors(int x, int y, int radius) {
        int counter = 0;
        int rightNeighborX = x + 1;
        if(checkGridBounds(rightNeighborX, y) && grid[rightNeighborX][y] == 1) counter++;
        return counter;
    }

    /*
     * Improved version of the countNeighbors function
     */
    private int countLiveNeighbors(int x, int y) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                
                int nx = x + dx;
                int ny = y + dy;
                
                // Wrap around edges (toroidal grid)
                nx = (nx + GRID_WIDTH) % GRID_WIDTH;
                ny = (ny + GRID_HEIGHT) % GRID_HEIGHT;
                
                if (grid[nx][ny] == 1) count++;
            }
        }
        return count;
    }

    private boolean checkGridBounds(int x, int y) {
        return x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT;
    }

    private void drawGrid(GraphicsContext gc) {
        // Clear only the necessary area
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw cells with less overhead
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                switch(grid[x][y]){
                    case 1:
                        gc.setFill(Color.BLACK);
                        gc.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                        break;
                    case 2:
                        gc.setFill(Color.PALEGREEN);
                        gc.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                        break;
                }
            }
        }

        // Draw grid lines with lighter color and less opacity
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);
        for (int x = 0; x <= GRID_WIDTH; x++) {
            gc.strokeLine(x * CELL_SIZE, 0, x * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
        }
        for (int y = 0; y <= GRID_HEIGHT; y++) {
            gc.strokeLine(0, y * CELL_SIZE, GRID_WIDTH * CELL_SIZE, y * CELL_SIZE);
        }
    }

    private void handleMouseDraw(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        int gridX = (int) x / CELL_SIZE;
        int gridY = (int) y / CELL_SIZE;

        if (checkGridBounds(gridX, gridY)) {
            grid[gridX][gridY] = event.isPrimaryButtonDown() ? 2 : 0; // 1 if left click and 0 if right click
            drawGrid(canvas.getGraphicsContext2D());
        }

    }

    /* ------------ MAIN ------------ */
    public static void main(String[] args) {
        launch(args);
    }
}