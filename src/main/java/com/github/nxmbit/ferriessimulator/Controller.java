package com.github.nxmbit.ferriessimulator;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.fxml.Initializable;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private Simulation simulation;
    private Timeline timeline;
    private final SettingsImport settings = new SettingsImport();
    private final MapImport mapImport = new MapImport();
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
    @FXML
    private MenuItem menuStart;
    @FXML
    private MenuItem menuStop;
    @FXML
    private Button spawnLeftButton;
    @FXML
    private Button spawnRightButton;
    @FXML
    private Slider minVehicleSpeedSlider;
    @FXML
    private Label minVehicleSpeedLabel;
    @FXML
    private Slider maxVehicleSpeedSlider;
    @FXML
    private Label maxVehicleSpeedLabel;
    @FXML
    private Spinner<Integer> dock1EntryQueueSpinner;
    @FXML
    private Spinner<Integer> dock1ExitQueueSpinner;
    @FXML
    private Spinner<Integer> dock2EntryQueueSpinner;
    @FXML
    private Spinner<Integer> dock2ExitQueueSpinner;
    @FXML
    private Slider ferrySpeedSlider;
    @FXML
    private Label ferrySpeedLabel;
    @FXML
    private Slider minFerryLoadingTimeSlider;
    @FXML
    private Label minFerryLoadingTimeLabel;
    @FXML
    private Slider maxFerryLoadingTimeSlider;
    @FXML
    private Label maxFerryLoadingTimeLabel;
    @FXML
    private Spinner<Integer> leftFerriesSpinner;
    @FXML
    private Label leftFerriesLabel;
    @FXML
    private Spinner<Integer> rightFerriesSpinner;
    @FXML
    private Label rightFerriesLabel;
    @FXML
    private Slider minFerryCapacitySlider;
    @FXML
    private Label minFerryCapacityLabel;
    @FXML
    private Slider maxFerryCapacitySlider;
    @FXML
    private Label maxFerryCapacityLabel;


    private Tile[][] grid;
    private TileType[][] OriginalTileTypes;

    private int gridWidth;
    private int gridHeight;
    private int dockHeight;
    private double tileSize;

    private int dock1EnteringCapacity = mapImport.getDock1EnteringCapacity();
    private int dock1ExitingCapacity = mapImport.getDock1ExitingCapacity();
    private int dock2EnteringCapacity = mapImport.getDock2EnteringCapacity();
    private int dock2ExitingCapacity = mapImport.getDock2ExitingCapacity();

    private int minFerryCapacity = settings.getMinRandomFerryCapacity();
    private int maxFerryCapacity = settings.getMaxRandomFerryCapacity();
    private int minFerryLoadingTime = settings.getMinRandomFerryLoadingTime();
    private int maxFerryLoadingTime = settings.getMaxRandomFerryLoadingTime();
    private int leftFerries = mapImport.getDock1FerryQueueSize();
    private int rightFerries = mapImport.getDock2FerryQueueSize();
    private double ferrySpeed = settings.getFerrySpeed();



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pane.widthProperty().addListener((obs, oldVal, newVal) -> setupIfReady());
        pane.heightProperty().addListener((obs, oldVal, newVal) -> setupIfReady());
        setupSpawnIntervalSlider();
        setupMaxVehiclesSpinner();
        setupDockSpawnProbabilitySlider();
        setupMinVehicleSpeedSlider();
        setupMaxVehicleSpeedSlider();
        setupQueueSpinners();
        setupFerrySettings();
        setupControlsFromSettings();
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

    private void setupControlsFromSettings() {
        // Set default values from settings
        spawnIntervalSlider.setValue(settings.getVehiclesSpawnInterval() / 1000.0);
        maxVehiclesSpinner.getValueFactory().setValue(settings.getMaxVehicles());
        dockSpawnProbabilitySlider.setValue(settings.getLeftRightDockSpawnBalance() * 100);

        minVehicleSpeedSlider.setValue(settings.getMinRandomVehicleSpeed());
        maxVehicleSpeedSlider.setValue(settings.getMaxRandomVehicleSpeed());

        leftDockProbabilityLabel.setText(String.format("Left Dock Probability: %.0f%%", settings.getLeftRightDockSpawnBalance() * 100));
        rightDockProbabilityLabel.setText(String.format("Right Dock Probability: %.0f%%", (1 - settings.getLeftRightDockSpawnBalance()) * 100));

        minVehicleSpeedLabel.setText("Min Speed: " + settings.getMinRandomVehicleSpeed());
        maxVehicleSpeedLabel.setText("Max Speed: " + settings.getMaxRandomVehicleSpeed());

        minFerryLoadingTimeSlider.setValue(settings.getMinRandomFerryLoadingTime() / 1000.0);
        maxFerryLoadingTimeSlider.setValue(settings.getMaxRandomFerryLoadingTime() / 1000.0);

        minFerryLoadingTimeLabel.setText("Min Loading Time: " + settings.getMinRandomFerryLoadingTime() / 1000 + " s");
        maxFerryLoadingTimeLabel.setText("Max Loading Time: " + settings.getMaxRandomFerryLoadingTime() / 1000 + " s");

        ferrySpeedSlider.setValue(settings.getFerrySpeed());

        leftFerriesLabel.setText("Number of Ferries on Left: " + mapImport.getDock1FerryQueueSize());
        rightFerriesLabel.setText("Number of Ferries on Right: " + mapImport.getDock2FerryQueueSize());
        leftFerriesSpinner.getValueFactory().setValue(mapImport.getDock1FerryQueueSize());
        rightFerriesSpinner.getValueFactory().setValue(mapImport.getDock2FerryQueueSize());

        minFerryCapacitySlider.setValue(settings.getMinRandomFerryCapacity());
        maxFerryCapacitySlider.setValue(settings.getMaxRandomFerryCapacity());

        minFerryCapacityLabel.setText("Min Ferry Capacity: " + settings.getMinRandomFerryCapacity());
        maxFerryCapacityLabel.setText("Max Ferry Capacity: " + settings.getMaxRandomFerryCapacity());

        dock1EntryQueueSpinner.getValueFactory().setValue(mapImport.getDock1EnteringCapacity());
        dock1ExitQueueSpinner.getValueFactory().setValue(mapImport.getDock1ExitingCapacity());
        dock2EntryQueueSpinner.getValueFactory().setValue(mapImport.getDock2EnteringCapacity());
        dock2ExitQueueSpinner.getValueFactory().setValue(mapImport.getDock2ExitingCapacity());

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

    public void setupSimulation() {
        simulation = new Simulation();
        System.out.println("tileSize: " + tileSize);
        simulation.setup(dockHeight, tileSize, grid, OriginalTileTypes, dock1EnteringCapacity, dock1ExitingCapacity,
                dock2EnteringCapacity, dock2ExitingCapacity, ferrySpeed, leftFerries, rightFerries, minFerryLoadingTime,
                maxFerryLoadingTime, minFerryCapacity, maxFerryCapacity);
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
            setupControlsFromSettings();
            updateUIState();
        }
    }

    private void clearSimulation() {
        pane.getChildren().clear();
        createGrid();
        draw();
    }

    private void updateUIState() {
        startButton.setDisable(simulationRunning);
        stopButton.setDisable(!simulationRunning);
        menuStart.setDisable(simulationRunning);
        menuStop.setDisable(!simulationRunning);

        spawnIntervalSlider.setDisable(!simulationRunning);
        maxVehiclesSpinner.setDisable(!simulationRunning);
        dockSpawnProbabilitySlider.setDisable(!simulationRunning);
        spawnLeftButton.setDisable(!simulationRunning);
        spawnRightButton.setDisable(!simulationRunning);
        minVehicleSpeedSlider.setDisable(!simulationRunning);
        maxVehicleSpeedSlider.setDisable(!simulationRunning);

        dock1EntryQueueSpinner.setDisable(simulationRunning);
        dock1ExitQueueSpinner.setDisable(simulationRunning);
        dock2EntryQueueSpinner.setDisable(simulationRunning);
        dock2ExitQueueSpinner.setDisable(simulationRunning);

        ferrySpeedSlider.setDisable(simulationRunning);
        minFerryLoadingTimeSlider.setDisable(simulationRunning);
        maxFerryLoadingTimeSlider.setDisable(simulationRunning);
        leftFerriesSpinner.setDisable(simulationRunning);
        rightFerriesSpinner.setDisable(simulationRunning);
        minFerryCapacitySlider.setDisable(simulationRunning);
        maxFerryCapacitySlider.setDisable(simulationRunning);
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

    private void draw() {
        pane.getChildren().clear();
        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                Tile tile = grid[i][j];
                pane.getChildren().add(tile);
            }
        }

        if (simulation != null) {
                for (Ferry ferry : simulation.getFerries()) {
                    pane.getChildren().add(ferry);
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

    private void setupMinVehicleSpeedSlider() {
        minVehicleSpeedSlider.setMajorTickUnit(1);
        minVehicleSpeedSlider.setMinorTickCount(0);
        minVehicleSpeedSlider.setSnapToTicks(true);
        minVehicleSpeedSlider.setShowTickLabels(true);
        minVehicleSpeedSlider.setShowTickMarks(true);

        minVehicleSpeedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() > maxVehicleSpeedSlider.getValue()) {
                minVehicleSpeedSlider.setValue(maxVehicleSpeedSlider.getValue());
            } else {
                minVehicleSpeedLabel.setText("Min Speed: " + newValue);
                if (simulation != null && simulationRunning) {
                    simulation.getVehicleSpawner().setMinSpeed(newValue.doubleValue());
                }
            }
        });
    }

    private void setupMaxVehicleSpeedSlider() {
        maxVehicleSpeedSlider.setMajorTickUnit(1);
        maxVehicleSpeedSlider.setMinorTickCount(0);
        maxVehicleSpeedSlider.setSnapToTicks(true);
        maxVehicleSpeedSlider.setShowTickLabels(true);
        maxVehicleSpeedSlider.setShowTickMarks(true);

        maxVehicleSpeedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() < minVehicleSpeedSlider.getValue()) {
                maxVehicleSpeedSlider.setValue(minVehicleSpeedSlider.getValue());
            } else {
                maxVehicleSpeedLabel.setText("Max Speed: " + newValue);
                if (simulation != null && simulationRunning) {
                    simulation.getVehicleSpawner().setMaxSpeed(newValue.doubleValue());
                }
            }
        });
    }

    private void setupQueueSpinners() {
        // Initialize the MapImport object
        MapImport mapImport = new MapImport();

        // Left dock entry queue spinner
        SpinnerValueFactory<Integer> dock1EntryQueueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, mapImport.getDock1EnteringCapacity(), mapImport.getDock1EnteringCapacity());
        dock1EntryQueueSpinner.setValueFactory(dock1EntryQueueFactory);

        // Left dock exit queue spinner
        SpinnerValueFactory<Integer> dock1ExitQueueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, mapImport.getDock1ExitingCapacity(), mapImport.getDock1ExitingCapacity());
        dock1ExitQueueSpinner.setValueFactory(dock1ExitQueueFactory);

        // Right dock entry queue spinner
        SpinnerValueFactory<Integer> dock2EntryQueueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, mapImport.getDock2EnteringCapacity(), mapImport.getDock2EnteringCapacity());
        dock2EntryQueueSpinner.setValueFactory(dock2EntryQueueFactory);

        // Right dock exit queue spinner
        SpinnerValueFactory<Integer> dock2ExitQueueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, mapImport.getDock2ExitingCapacity(), mapImport.getDock2ExitingCapacity());
        dock2ExitQueueSpinner.setValueFactory(dock2ExitQueueFactory);

        // Add listeners to spinners to update the queue sizes in simulation
        dock1EntryQueueSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            dock1EnteringCapacity = newValue;
        });

        dock1ExitQueueSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            dock1ExitingCapacity = newValue;
        });

        dock2EntryQueueSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            dock2EnteringCapacity = newValue;
        });

        dock2ExitQueueSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            dock2ExitingCapacity = newValue;
        });
    }

    private void setupFerrySettings() {
        ferrySpeedSlider.setMajorTickUnit(1);
        ferrySpeedSlider.setMinorTickCount(0);
        ferrySpeedSlider.setSnapToTicks(true);
        ferrySpeedSlider.setShowTickLabels(true);
        ferrySpeedSlider.setShowTickMarks(true);

        ferrySpeedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ferrySpeedLabel.setText("Speed: " + newValue.toString());
            ferrySpeed = newValue.doubleValue();
        });

        ferrySpeedSlider.setValue(settings.getFerrySpeed());

        minFerryLoadingTimeSlider.setMajorTickUnit(1);
        minFerryLoadingTimeSlider.setMinorTickCount(0);
        minFerryLoadingTimeSlider.setSnapToTicks(true);
        minFerryLoadingTimeSlider.setShowTickLabels(false);
        minFerryLoadingTimeSlider.setShowTickMarks(true);

        maxFerryLoadingTimeSlider.setMajorTickUnit(1);
        maxFerryLoadingTimeSlider.setMinorTickCount(0);
        maxFerryLoadingTimeSlider.setSnapToTicks(true);
        maxFerryLoadingTimeSlider.setShowTickLabels(false);
        maxFerryLoadingTimeSlider.setShowTickMarks(true);

        minFerryLoadingTimeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() > maxFerryLoadingTimeSlider.getValue()) {
                minFerryLoadingTimeSlider.setValue(maxFerryLoadingTimeSlider.getValue());
            } else {
                minFerryLoadingTimeLabel.setText("Min Loading Time: " + newValue.intValue() + " s");
                minFerryLoadingTime = newValue.intValue();
            }
        });

        maxFerryLoadingTimeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() < minFerryLoadingTimeSlider.getValue()) {
                maxFerryLoadingTimeSlider.setValue(minFerryLoadingTimeSlider.getValue());
            } else {
                maxFerryLoadingTimeLabel.setText("Max Loading Time: " + newValue.intValue() + " s");
                maxFerryLoadingTime = newValue.intValue();
            }
        });

        minFerryLoadingTimeSlider.setValue(settings.getMinRandomFerryLoadingTime() / 1000.0);
        maxFerryLoadingTimeSlider.setValue(settings.getMaxRandomFerryLoadingTime() / 1000.0);

        SpinnerValueFactory<Integer> leftFerriesFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, mapImport.getDock1FerryQueueSize(), mapImport.getDock1FerryQueueSize());
        leftFerriesSpinner.setValueFactory(leftFerriesFactory);

        SpinnerValueFactory<Integer> rightFerriesFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, mapImport.getDock2FerryQueueSize(), mapImport.getDock2FerryQueueSize());
        rightFerriesSpinner.setValueFactory(rightFerriesFactory);

        leftFerriesSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            leftFerriesLabel.setText("Number of Ferries on Left: " + newValue.intValue());
            leftFerries = newValue.intValue();
        });

        rightFerriesSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            rightFerriesLabel.setText("Number of Ferries on Right: " + newValue.intValue());
            rightFerries = newValue.intValue();
        });

        minFerryCapacitySlider.setMajorTickUnit(1);
        minFerryCapacitySlider.setMinorTickCount(0);
        minFerryCapacitySlider.setSnapToTicks(true);
        minFerryCapacitySlider.setShowTickLabels(true);
        minFerryCapacitySlider.setShowTickMarks(true);

        maxFerryCapacitySlider.setMajorTickUnit(1);
        maxFerryCapacitySlider.setMinorTickCount(0);
        maxFerryCapacitySlider.setSnapToTicks(true);
        maxFerryCapacitySlider.setShowTickLabels(true);
        maxFerryCapacitySlider.setShowTickMarks(true);

        minFerryCapacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() > maxFerryCapacitySlider.getValue()) {
                minFerryCapacitySlider.setValue(maxFerryCapacitySlider.getValue());
            } else {
                minFerryCapacityLabel.setText("Min Ferry Capacity: " + newValue.intValue());
                minFerryCapacity = newValue.intValue();
            }
        });

        maxFerryCapacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() < minFerryCapacitySlider.getValue()) {
                maxFerryCapacitySlider.setValue(minFerryCapacitySlider.getValue());
            } else {
                maxFerryCapacityLabel.setText("Max Ferry Capacity: " + newValue.intValue());
                maxFerryCapacity = newValue.intValue();
            }
        });

        minFerryCapacitySlider.setValue(settings.getMinRandomFerryCapacity());
        maxFerryCapacitySlider.setValue(settings.getMaxRandomFerryCapacity());


    }


    @FXML
    private void spawnLeftVehicle() {
        if (simulationRunning) {
            simulation.getVehicleSpawner().trySpawnVehicleOnDock(1);
        }
    }

    @FXML
    private void spawnRightVehicle() {
        if (simulationRunning) {
            simulation.getVehicleSpawner().trySpawnVehicleOnDock(2);
        }
    }

    @FXML
    private void setLightTheme() {
        Stage stage = (Stage) pane.getScene().getWindow();
        stage.getScene().getStylesheets().remove(getClass().getResource("dark-theme.css").toExternalForm());
        stage.getScene().getStylesheets().add(getClass().getResource("light-theme.css").toExternalForm());
    }

    @FXML
    private void setDarkTheme() {
        Stage stage = (Stage) pane.getScene().getWindow();
        stage.getScene().getStylesheets().remove(getClass().getResource("light-theme.css").toExternalForm());
        stage.getScene().getStylesheets().add(getClass().getResource("dark-theme.css").toExternalForm());
    }

    @FXML
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Symulator promów na rzece\nPaweł Hołownia WCY22IY3S1");
        alert.setContentText("Realizowane zadanie:\nPromy na rzece.\n" +
                "Założenia:\n" +
                "Przeprawę obsługuje N promów o określonej pojemności każdy.\n" +
                "Na przystań i z przystani prowadzi jedna droga, po której poruszają się pojazdy z różną\n" +
                "prędkością.\n" +
                "Przystań ma ograniczoną pojemność.\n" +
                "Na promy jest tylko jeden wspólny wjazd i zjazd, przez który samochody wjeżdżają/zjeżdżają\n" +
                "pojedynczo (pierwszeństwo samochodów zjeżdżających).\n" +
                "Promy czekają na zapełnienie przez określony maksymalny czas, po którym:\n" +
                "- rozpoczynają przeprawę, gdy na pokładzie znajduje się co najmniej jeden pojazd,\n" +
                "- lub rozpoczynają kolejny okres oczekiwania.\n" +
                "Jeżeli zapełnią się wcześniej, to rozpoczynają przeprawę przed upływem tego czasu.\n" +
                "W przypadku przybycia promu do przystani, gdy jest zajęta przez inny prom, to oczekuje w\n" +
                "kolejce.");

        alert.showAndWait();
    }

}

