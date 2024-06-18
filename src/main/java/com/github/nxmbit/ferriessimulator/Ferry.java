package com.github.nxmbit.ferriessimulator;

import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class Ferry extends Pane implements Runnable {
    private double speed;
    private int capacity;
    private Dock currentDock;
    private Dock targetDock;
    private int maxLoadingTime;
    private FerryState state;
    private double tileSize;
    private ConcurrentLinkedQueue<Vehicle> vehiclesOnBoard;
    private Semaphore vehicleSemaphore;
    private Rectangle ferryRectangle;
    private Label vehicleCountLabel;
    private Label capacityLabel;
    private Label loadingTimeLabel;
    private int dockHeight;
    private long lastUpdateTime;

    private AtomicBoolean running;

    private MovementState movementState;

    public Ferry(double speed, int capacity, Dock currentDock, Dock targetDock, int maxLoadingTime, double tileSize, int dockHeight){
        this.speed = speed;
        this.capacity = capacity;
        this.currentDock = currentDock;
        this.targetDock = targetDock;
        this.maxLoadingTime = maxLoadingTime;
        this.tileSize = tileSize;
        this.state = FerryState.LOADING;
        this.vehiclesOnBoard = new ConcurrentLinkedQueue<>();
        this.vehicleSemaphore = new Semaphore(capacity, true);
        this.movementState = MovementState.AT_DOCK;
        this.dockHeight = dockHeight;
        this.running = new AtomicBoolean(true);
        initializeFerry();
    }

    private void initializeFerry() {
        double ferryWidth = tileSize * dockHeight;
        double ferryHeight = tileSize * dockHeight;

        ferryRectangle = new Rectangle(ferryWidth, ferryHeight, Color.DARKGRAY);
        vehicleCountLabel = new Label("0");
        vehicleCountLabel.setTextFill(Color.WHITE);

        capacityLabel = new Label(String.valueOf(capacity));
        capacityLabel.setTextFill(Color.RED);

        loadingTimeLabel = new Label("0");
        loadingTimeLabel.setTextFill(Color.WHITE);

        getChildren().addAll(ferryRectangle, vehicleCountLabel, capacityLabel, loadingTimeLabel);

        setLayoutX(currentDock.getFerryCoordinateX() * tileSize);
        setLayoutY(currentDock.getFerryCoordinateY() * tileSize);

        positionLabels();
    }

    private void positionLabels() {
        capacityLabel.setLayoutX(5);
        capacityLabel.setLayoutY(5);

        vehicleCountLabel.setLayoutX(ferryRectangle.getWidth() / 2 - 10);
        vehicleCountLabel.setLayoutY(ferryRectangle.getHeight() / 2 - 10);

        loadingTimeLabel.setLayoutX(ferryRectangle.getWidth() / 2 - 10);
        loadingTimeLabel.setLayoutY(ferryRectangle.getHeight() - 20);
    }

    public void stop() {
        running.set(false);
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                Thread.sleep(100);
                updateFerryState();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateFerryState() {
        switch (state) {
            case LOADING:
                loadVehicles();
                break;
            case TRAVELING:
                travel();
                break;
            case UNLOADING:
                unloadVehicles();
                break;
        }
    }

    private void loadVehicles() {
        currentDock.setFerryAtDock(true);
        long startTime = System.currentTimeMillis();
        loadingTimeLabel.setVisible(true);
        while ((System.currentTimeMillis() - startTime < maxLoadingTime) && running.get()) {
            if (vehicleSemaphore.availablePermits() == 0) {
                System.out.println("Prom pełny");
                break;
            }

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdateTime >= 1000) {
                long remainingTime = maxLoadingTime - (currentTime - startTime);
                updateLoadingTimeLabel(remainingTime);
                lastUpdateTime = currentTime;
            }

            currentDock.signalVehicleToEnterCriticalSection(); // Signal to the next vehicle in the entering queue
            currentDock.getCriticalSectionLock().lock();
            try {
                Vehicle vehicle = currentDock.getCriticalSectionVehicle();
                if (vehicle != null && vehicle.getTravelState() == VehicleState.LOADING_ON_FERRY) {
                    try {
                        vehicleSemaphore.acquire();
                        vehiclesOnBoard.add(vehicle);
                        currentDock.setCriticalSectionVehicle(null);
                        vehicle.revertChangesToCriticalSectionWhenBoarding();
                        updateVehicleCount();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            } finally {
                currentDock.getCriticalSectionLock().unlock();
            }
        }

        if (vehiclesOnBoard.isEmpty()) {
            state = FerryState.LOADING; // Restart loading period if no vehicles on board
        } else {
            currentDock.setFerryAtDock(false);
            loadingTimeLabel.setVisible(false); // Hide the label after loading
            state = FerryState.TRAVELING;
        }
    }

    private void unloadVehicles() {
        while (!vehiclesOnBoard.isEmpty()) {
            Vehicle vehicle = vehiclesOnBoard.poll();
            if (vehicle != null) {
                currentDock.getCriticalSectionLock().lock();
                try {
                    currentDock.setCriticalSectionVehicle(vehicle);
                    vehicle.setTravelState(VehicleState.UNLOADING_FROM_FERRY);
                    updateVehicleCount();
                    vehicleSemaphore.release();
                } finally {
                    currentDock.getCriticalSectionLock().unlock();
                }
            }
        }
        state = FerryState.LOADING;
    }

    private void travel() {
        double targetX;
        double targetY;

        switch (movementState) {
            case GO_TO_LANE_START:
                targetX = currentDock.getLaneToNextDockStartX() * tileSize;
                targetY = currentDock.getLaneToNextDockStartY() * tileSize;
                moveTo(targetX, targetY);

                if (hasReachedTarget(targetX, targetY)) {
                    movementState = MovementState.GO_TO_LANE_END;
                    setLayoutX(targetX);
                    setLayoutY(targetY);
                }
                break;

            case GO_TO_LANE_END:
                targetX = currentDock.getLaneToNextDockEndX() * tileSize;
                targetY = currentDock.getLaneToNextDockEndY() * tileSize;
                moveTo(targetX, targetY);

                if (hasReachedTarget(targetX, targetY)) {
                    movementState = MovementState.GO_TO_DOCK;
                    setLayoutX(targetX);
                    setLayoutY(targetY);
                }
                break;

            case GO_TO_DOCK:
                targetX = targetDock.getFerryCoordinateX() * tileSize;
                targetY = targetDock.getFerryCoordinateY() * tileSize;
                moveTo(targetX, targetY);

                if (hasReachedTarget(targetX, targetY)) {
                    movementState = MovementState.AT_DOCK;
                    state = FerryState.UNLOADING;
                    Dock temp = currentDock;
                    currentDock = targetDock;
                    targetDock = temp;
                    setLayoutX(targetX);
                    setLayoutY(targetY);
                }
                break;

            case AT_DOCK:
                movementState = MovementState.GO_TO_LANE_START;
                break;
        }
    }

    private void moveTo(double targetX, double targetY) {
        double deltaX = targetX - getLayoutX();
        double deltaY = targetY - getLayoutY();
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (distance > speed) {
            double stepX = speed * (deltaX / distance);
            double stepY = speed * (deltaY / distance);

            setLayoutX(getLayoutX() + stepX);
            setLayoutY(getLayoutY() + stepY);
        } else {
            setLayoutX(targetX);
            setLayoutY(targetY);
        }
    }

    private boolean hasReachedTarget(double targetX, double targetY) {
        boolean reached = Math.abs(getLayoutX() - targetX) < 1 && Math.abs(getLayoutY() - targetY) < 1;
        return reached;
    }


    private void updateVehicleCount() {
        Platform.runLater(() -> vehicleCountLabel.setText(String.valueOf(vehiclesOnBoard.size())));
    }

    private void updateLoadingTimeLabel(long remainingTime) {
        Platform.runLater(() -> loadingTimeLabel.setText(String.valueOf(remainingTime / 1000) + " s"));
    }
}

enum FerryState {
    LOADING,
    TRAVELING,
    UNLOADING,
}

enum MovementState {
    AT_DOCK,
    GO_TO_LANE_START,
    GO_TO_LANE_END,
    GO_TO_DOCK,
    IN_QUEUE
}
