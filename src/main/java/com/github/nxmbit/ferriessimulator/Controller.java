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

    private final int gridWidth = 128;
    private final int gridHeight = 64;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createGrid();
        draw();
        setupSimulation();
        updateUIState();

        toggleGridButton.selectedProperty().addListener((obs, oldVal, newVal) -> toggleGridVisibility());
    }

    public void setupSimulation() {
        simulation = new Simulation();
        simulation.setup(2, 10, pane.getWidth(), pane.getHeight(), 100, 100, 50, 5, grid, OriginalTileTypes);
        timeline = new Timeline(new KeyFrame(Duration.millis(90), e -> draw()));
        timeline.setCycleCount(Timeline.INDEFINITE);

        pane.widthProperty().addListener((obs, oldVal, newVal) -> resizeGrid());
        pane.heightProperty().addListener((obs, oldVal, newVal) -> resizeGrid());

    }

    public void startSimulation() {
        if (!simulationRunning) {
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
            simulationRunning = false;
            updateUIState();
        }
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
    }

    private void createGrid() {
        MapImport gridGenerator = new MapImport();
        double tileSize = Math.max(pane.getWidth() / gridWidth, pane.getHeight() / gridHeight);
        grid = gridGenerator.generate(gridWidth, gridHeight, tileSize);
        OriginalTileTypes = gridGenerator.getOriginalTileTypes();
    }

    private void resizeGrid() {
        double tileSize = Math.min(pane.getWidth() / gridWidth, pane.getHeight() / gridHeight);

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
