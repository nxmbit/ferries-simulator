package com.github.nxmbit.ferriessimulator;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private Simulation simulation;
    private Timeline timeline;
    private boolean simulationRunning = false;

    @FXML
    private Pane pane;
    @FXML
    private Slider spawnIntervalSlider;
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button setIntervalButton;
    @FXML
    private Button spawnVehicleButton;
    @FXML
    private TextField maxVehiclesField;
    @FXML
    private Button setMaxVehiclesButton;
    @FXML
    private ToggleButton toggleGridButton;

    private Tile[][] grid;
    private TileType[][] OriginalTileTypes;

    private int gridWidth;
    private int gridHeight;
    private int dockHeight;
    private double tileSize;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pane.widthProperty().addListener((obs, oldVal, newVal) -> setupIfReady());
        pane.heightProperty().addListener((obs, oldVal, newVal) -> setupIfReady());
    }

    private void setupIfReady() {
        if (pane.getWidth() > 0 && pane.getHeight() > 0) {
            createGrid();
            draw();
            setupSimulation();
            updateUIState();
        }
    }

    public void setupSimulation() {
        simulation = new Simulation();
        System.out.println("tileSize: " + tileSize);
        simulation.setup(2, dockHeight, tileSize, 8000, grid, OriginalTileTypes);
        timeline = new Timeline(new KeyFrame(Duration.millis(90), e -> draw()));
        timeline.setCycleCount(Timeline.INDEFINITE);

        pane.widthProperty().addListener((obs, oldVal, newVal) -> resizeGrid());
        pane.heightProperty().addListener((obs, oldVal, newVal) -> resizeGrid());

    }

    public void startSimulation() {
        if (!simulationRunning) {
            setupSimulation();
            new Thread(simulation).start();
            timeline.play();
            simulationRunning = true;
            updateUIState();
        }
    }

    public void stopSimulation() {
        if (simulationRunning) {
            timeline.stop();
            simulation.stop();
            clearSimulation();
            simulationRunning = false;
            updateUIState();
        }
    }

    private void clearSimulation() {
        pane.getChildren().clear();
        createGrid();
        draw();
    }

    public void adjustSpawnInterval() {
        if (simulationRunning) {
            long interval = (long) spawnIntervalSlider.getValue();
            simulation.getVehicleSpawner().setSpawnInterval(interval);
        }
    }

    @FXML
    public void setMaxVehicles() {
        if (simulationRunning) {
            try {
                int maxVehicles = Integer.parseInt(maxVehiclesField.getText());
                simulation.getVehicleSpawner().setMaxCars(maxVehicles);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format for max vehicles");
            }
        }
    }

    public void spawnVehicle() {
        if (simulationRunning) {
            simulation.getVehicleSpawner().trySpawnVehicle();
        }
    }

    private void updateUIState() {
        startButton.setDisable(simulationRunning);
        stopButton.setDisable(!simulationRunning);
        setIntervalButton.setDisable(!simulationRunning);
        spawnVehicleButton.setDisable(!simulationRunning);
        spawnIntervalSlider.setDisable(!simulationRunning);
        maxVehiclesField.setDisable(!simulationRunning);
        setMaxVehiclesButton.setDisable(!simulationRunning);
    }

    public void toggleGridVisibility() {
        boolean isGridVisible = toggleGridButton.isSelected();
        for (Tile[] tileRow : grid) {
            for (Tile tile : tileRow) {
                tile.setStrokeVisibility(isGridVisible);
            }
        }
        draw();
    }

    public Pane getPane() {
        return pane;
    }

    private void draw() {
        pane.getChildren().clear();
        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                Tile tile = grid[i][j];
                pane.getChildren().add(tile);
            }
        }

        //draw ferries
        if (simulation != null) {
            synchronized (simulation.getFerries()) {
                for (Ferry ferry : simulation.getFerries()) {
                    pane.getChildren().add(ferry);
                }
            }
        }
    }

    private void createGrid() {
        MapImport gridGenerator = new MapImport();
        gridWidth = gridGenerator.getGridWidth();
        gridHeight = gridGenerator.getGridHeight();
        dockHeight = gridGenerator.getDockHeight();
        tileSize = Math.min(pane.getWidth() / gridWidth, pane.getHeight() / gridHeight);
        grid = gridGenerator.generate(gridWidth, gridHeight, tileSize);
        OriginalTileTypes = gridGenerator.getOriginalTileTypes();
    }

    private void resizeGrid() {
        tileSize = Math.min(pane.getWidth() / gridWidth, pane.getHeight() / gridHeight);

        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                Tile tile = grid[i][j];
                tile.setWidth(tileSize);
                tile.setHeight(tileSize);
                tile.setX(i * tileSize);
                tile.setY(j * tileSize);
            }
        }
    }
}

