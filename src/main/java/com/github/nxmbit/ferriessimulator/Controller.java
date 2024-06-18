package com.github.nxmbit.ferriessimulator;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.fxml.Initializable;
import javafx.util.StringConverter;

import java.net.URL;
import java.security.cert.PolicyNode;
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
    private Spinner<Integer> maxVehiclesSpinner;
    @FXML
    private ToggleButton toggleGridButton;
    @FXML
    private VBox accordionVBox;
    @FXML
    private Slider dockSpawnProbabilitySlider;
    @FXML
    private Label leftDockProbabilityLabel;
    @FXML
    private Label rightDockProbabilityLabel;

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
        //setupTitledPaneListeners();
        setupSpawnIntervalSlider();
        setupMaxVehiclesSpinner();
        setupDockSpawnProbabilitySlider();
    }

    private void setupIfReady() {
        if (pane.getWidth() > 0 && pane.getHeight() > 0) {
            createGrid();
            draw();
            setupSimulation();
            updateUIState();
        }
    }

    private void setupSpawnIntervalSlider() {
        spawnIntervalSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                return String.format("%.0f", object);
            }

            @Override
            public Double fromString(String string) {
                return Double.valueOf(string);
            }
        });

        spawnIntervalSlider.setMajorTickUnit(1);
        spawnIntervalSlider.setMinorTickCount(0);
        spawnIntervalSlider.setSnapToTicks(true);
        spawnIntervalSlider.setShowTickLabels(true);
        spawnIntervalSlider.setShowTickMarks(true);

        spawnIntervalSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (simulation != null && simulationRunning) {
                simulation.getVehicleSpawner().setSpawnInterval(newValue.longValue() * 1000);
            }
        });
    }

    private void setupDockSpawnProbabilitySlider() {
        dockSpawnProbabilitySlider.setMin(0);
        dockSpawnProbabilitySlider.setMax(100);
        dockSpawnProbabilitySlider.setMajorTickUnit(10);
        dockSpawnProbabilitySlider.setMinorTickCount(1);
        dockSpawnProbabilitySlider.setSnapToTicks(true);
        dockSpawnProbabilitySlider.setShowTickMarks(true);
        dockSpawnProbabilitySlider.setShowTickLabels(true);

        dockSpawnProbabilitySlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                return String.format("%.0f%%", object);
            }

            @Override
            public Double fromString(String string) {
                return Double.valueOf(string.replace("%", ""));
            }
        });

        dockSpawnProbabilitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double leftDockProbability = newValue.doubleValue() / 100.0;
            double rightDockProbability = 1 - leftDockProbability;
            leftDockProbabilityLabel.setText(String.format("Left Dock Probability: %.0f%%", leftDockProbability * 100));
            rightDockProbabilityLabel.setText(String.format("Right Dock Probability: %.0f%%", rightDockProbability * 100));
            if (simulation != null && simulationRunning) {
                simulation.getVehicleSpawner().setDockSpawnProbability(leftDockProbability);
            }
        });
    }

    private void setupMaxVehiclesSpinner() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 5);
        maxVehiclesSpinner.setValueFactory(valueFactory);

        maxVehiclesSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (simulation != null && simulationRunning) {
                simulation.getVehicleSpawner().setMaxCars(newValue);
            }
        });
    }

// collapse all titled panes except the one that was clicked
//    private void setupTitledPaneListeners() {
//        for (javafx.scene.Node node : accordionVBox.getChildren()) {
//            if (node instanceof TitledPane) {
//                TitledPane titledPane = (TitledPane) node;
//                titledPane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
//                    if (isNowExpanded) {
//                        for (javafx.scene.Node otherNode : accordionVBox.getChildren()) {
//                            if (otherNode instanceof TitledPane && otherNode != titledPane) {
//                                ((TitledPane) otherNode).setExpanded(false);
//                            }
//                        }
//                    }
//                });
//            }
//        }
//    }

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


    public void spawnVehicle() {
        if (simulationRunning) {
            simulation.getVehicleSpawner().trySpawnVehicle();
        }
    }

    private void updateUIState() {
        startButton.setDisable(simulationRunning);
        stopButton.setDisable(!simulationRunning);
        //spawnVehicleButton.setDisable(!simulationRunning);
        spawnIntervalSlider.setDisable(!simulationRunning);
        maxVehiclesSpinner.setDisable(!simulationRunning);
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
            //synchronized (simulation.getFerries()) {
                for (Ferry ferry : simulation.getFerries()) {
                    pane.getChildren().add(ferry);
                }
            //}
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

