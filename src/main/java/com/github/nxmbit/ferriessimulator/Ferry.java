package com.github.nxmbit.ferriessimulator;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

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
    private int dockHeight;

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
        this.dockHeight = dockHeight;
        initializeFerry();
    }

    private void initializeFerry() {
        double ferryWidth = tileSize * dockHeight;
        double ferryHeight = tileSize * dockHeight;

        ferryRectangle = new Rectangle(ferryWidth, ferryHeight, Color.DARKGRAY);
        vehicleCountLabel = new Label("0");
        vehicleCountLabel.setTextFill(Color.WHITE);

        getChildren().addAll(ferryRectangle, vehicleCountLabel);

        setLayoutX(currentDock.getFerryCoordinateX() * tileSize);
        setLayoutY(currentDock.getFerryCoordinateY() * tileSize);

        System.out.println("koordynaty docku: " + currentDock.getFerryCoordinateX() + " " + currentDock.getFerryCoordinateY() + " " + tileSize);
        System.out.println("Inicjalizacja promu na współrzędnych: (" + getLayoutX() + ", " + getLayoutY() + ")");
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100);
                System.out.println(state);
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
        while (System.currentTimeMillis() - startTime < maxLoadingTime) {
            if (vehicleSemaphore.availablePermits() == 0) {
                System.out.println("Prom pełny");
                break;
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
        currentDock.setFerryAtDock(false);
        state = FerryState.TRAVELING;
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
        double targetX = targetDock.getFerryCoordinateX() * tileSize;
        double targetY = targetDock.getFerryCoordinateY() * tileSize;
        double deltaX = targetX - getLayoutX();
        double deltaY = targetY - getLayoutY();
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        double stepX = speed * (deltaX / distance);
        double stepY = speed * (deltaY / distance);

        setLayoutX(getLayoutX() + stepX);
        setLayoutY(getLayoutY() + stepY);

        if (distance <= speed) {
            setLayoutX(targetX);
            setLayoutY(targetY);
            Dock temp = currentDock;
            currentDock = targetDock;
            targetDock = temp;
            state = FerryState.UNLOADING;
        }
    }

    private void updateVehicleCount() {
        Platform.runLater(() -> vehicleCountLabel.setText(String.valueOf(vehiclesOnBoard.size())));
    }
}

enum FerryState {
    LOADING,
    TRAVELING,
    UNLOADING
}
