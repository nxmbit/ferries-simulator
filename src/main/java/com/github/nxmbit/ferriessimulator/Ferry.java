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
    private boolean atOtherQueueDockNotChangedYet;

    private AtomicBoolean running;

    private MovementState movementState;

    private static final double QUEUE_SPACING = 1.5;

    public Ferry(double speed, int capacity, Dock currentDock, Dock targetDock, int maxLoadingTime, double tileSize, int dockHeight){
        this.speed = speed;
        this.capacity = capacity;
        this.currentDock = currentDock;
        this.targetDock = targetDock;
        this.maxLoadingTime = maxLoadingTime;
        this.tileSize = tileSize;
        this.vehiclesOnBoard = new ConcurrentLinkedQueue<>();
        this.vehicleSemaphore = new Semaphore(capacity, true);
        this.movementState = MovementState.IN_QUEUE;
        this.state = FerryState.QUEUEING;
        this.dockHeight = dockHeight;
        this.running = new AtomicBoolean(true);
        this.atOtherQueueDockNotChangedYet = false;
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
            case QUEUEING:
                waitForDock();
                break;
        }
    }

    private void loadVehicles() {
        currentDock.setFerryAtDock(true);
        long startTime = System.currentTimeMillis();
        Platform.runLater(() -> loadingTimeLabel.setVisible(true));
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
            Platform.runLater(() -> loadingTimeLabel.setVisible(false));
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
                    currentDock.signalNextFerry();
                    movementState = MovementState.GO_TO_LANE_END;
                    safeSetLayoutX(targetX);
                    safeSetLayoutY(targetY);
                }
                break;

            case GO_TO_LANE_END:
                targetX = currentDock.getLaneToNextDockEndX() * tileSize;
                targetY = currentDock.getLaneToNextDockEndY() * tileSize;
                moveTo(targetX, targetY);

                if (targetDock.isFerryAtDock() || targetDock.peekNextFerryInQueue() != null) {
                    movementState = MovementState.PREPARING_TO_ENTER_QUEUE;
                }

                if (hasReachedTarget(targetX, targetY)) {
                    movementState = MovementState.GO_TO_DOCK;
                    safeSetLayoutX(targetX);
                    safeSetLayoutY(targetY);
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
                    atOtherQueueDockNotChangedYet = false;
                    safeSetLayoutX(targetX);
                    safeSetLayoutY(targetY);
                }
                break;

            case PREPARING_TO_ENTER_QUEUE:
                targetX = currentDock.getGoDownToNextDockQueueX() * tileSize;
                moveTo(targetX, getLayoutY());

                if (!targetDock.isFerryAtDock() && targetDock.peekNextFerryInQueue() == null) {
                    movementState = MovementState.GO_TO_LANE_END;
                }

                if (hasReachedTarget(targetX, getLayoutY())) {
                    movementState = MovementState.GO_DOWN_IN_QUEUE;
                    safeSetLayoutX(targetX);
                }

                break;

            case GO_DOWN_IN_QUEUE:
                targetY = targetDock.getFerryQueueCoordinateY() * tileSize +
                        targetDock.getFerryQueueSize() * dockHeight * QUEUE_SPACING * tileSize;
                moveTo(getLayoutX(), targetY);

                if (hasReachedTarget(getLayoutX(), targetY)) {
                    this.atOtherQueueDockNotChangedYet = true;
                    movementState = MovementState.ENTERING_QUEUE;
                }

                break;

            case ENTERING_QUEUE:
                enterQueue();
                break;

            case AT_DOCK:
                movementState = MovementState.GO_TO_LANE_START;
                break;

            case LEAVING_QUEUE:
                leaveQueue();
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

            safeSetLayoutX(getLayoutX() + stepX);
            safeSetLayoutY(getLayoutY() + stepY);
        } else {
            safeSetLayoutX(targetX);
            safeSetLayoutY(targetY);
        }
    }

    private boolean hasReachedTarget(double targetX, double targetY) {
        boolean reached = Math.abs(getLayoutX() - targetX) < 1 && Math.abs(getLayoutY() - targetY) < 1;
        return reached;
    }

    private void safeSetLayoutX(double x) {
        Platform.runLater(() -> setLayoutX(x));
    }

    private void safeSetLayoutY(double y) {
        Platform.runLater(() -> setLayoutY(y));
    }


    public boolean setQueuePosition(int positionInQueue) {
        if (movementState == MovementState.LEAVING_QUEUE) {
            return false;
        }

        double posX;
        double posY;

        if (!atOtherQueueDockNotChangedYet) {
            posX = currentDock.getFerryQueueCoordinateX() * tileSize;
            if (positionInQueue == 0) {
                posY = currentDock.getFerryQueueCoordinateY() * tileSize;
            } else {
                posY = (currentDock.getFerryQueueCoordinateY() + positionInQueue * dockHeight * QUEUE_SPACING) * tileSize;
            }
        } else {
            posX = targetDock.getFerryQueueCoordinateX() * tileSize;
            if (positionInQueue == 0) {
                posY = targetDock.getFerryQueueCoordinateY() * tileSize;
            } else {
                posY = (targetDock.getFerryQueueCoordinateY() + positionInQueue * dockHeight * QUEUE_SPACING) * tileSize;
            }
        }

        safeSetLayoutX(posX);
        safeSetLayoutY(posY);
        return true;
    }


    private void updateVehicleCount() {
        Platform.runLater(() -> vehicleCountLabel.setText(String.valueOf(vehiclesOnBoard.size())));
    }

    private void updateLoadingTimeLabel(long remainingTime) {
        Platform.runLater(() -> loadingTimeLabel.setText(String.valueOf(remainingTime / 1000) + " s"));
    }


    private void waitForDock() {
        if (atOtherQueueDockNotChangedYet) {
            targetDock.getDockLock().lock();
            try {
                if (targetDock.isFerryAtDock() || targetDock.peekNextFerryInQueue() != this) {
                    System.out.println(targetDock.peekNextFerryInQueue() + " is waiting for the dock");
                    targetDock.getDockAvailableCondition().await();
                }
                movementState = MovementState.LEAVING_QUEUE;
                state = FerryState.TRAVELING;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                targetDock.getDockLock().unlock();
            }

        } else {
            currentDock.getDockLock().lock();
            try {
                if (currentDock.isFerryAtDock() || currentDock.peekNextFerryInQueue() != this) {
                    System.out.println(currentDock.peekNextFerryInQueue() + " is waiting for the dock");
                    currentDock.getDockAvailableCondition().await();
                }
                movementState = MovementState.LEAVING_QUEUE;
                state = FerryState.TRAVELING;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                currentDock.getDockLock().unlock();
            }
        }
    }


    private void leaveQueue() {
        double targetX;
        double targetY;
        if (atOtherQueueDockNotChangedYet) {
            targetX = targetDock.getFerryCoordinateX() * tileSize;
            targetY = targetDock.getFerryCoordinateY() * tileSize;
            moveTo(targetX, targetY);

            if (hasReachedTarget(targetX, targetY)) {
                targetDock.pollFerryFromQueue();
                System.out.println("Ferry " + this + " has left the queue.");
                movementState = MovementState.AT_DOCK;
                state = FerryState.UNLOADING;
                targetDock.setFerryAtDock(true);
                Dock temp = currentDock;
                currentDock = targetDock;
                targetDock = temp;
                atOtherQueueDockNotChangedYet = false;
            }

        } else {
            targetX = currentDock.getFerryCoordinateX() * tileSize;
            targetY = currentDock.getFerryCoordinateY() * tileSize;
            moveTo(targetX, targetY);

            if (hasReachedTarget(targetX, targetY)) {
                currentDock.pollFerryFromQueue();
                System.out.println("Ferry " + this + " has reached the dock.");
                movementState = MovementState.AT_DOCK;
                state = FerryState.UNLOADING;
                currentDock.setFerryAtDock(true);
//                Dock temp = currentDock;
//                currentDock = targetDock;
//                targetDock = temp;
            }
        }
    }


    private void enterQueue() {
        System.out.println("Ferry " + this + " is entering the queue.");
        targetDock.addFerryToQueue(this);
        movementState = MovementState.IN_QUEUE;
        state = FerryState.QUEUEING;
    }

}

enum FerryState {
    LOADING,
    TRAVELING,
    UNLOADING,
    QUEUEING
}

enum MovementState {
    AT_DOCK,
    GO_TO_LANE_START,
    GO_TO_LANE_END,
    GO_TO_DOCK,
    IN_QUEUE,
    PREPARING_TO_ENTER_QUEUE,
    GO_DOWN_IN_QUEUE,
    LEAVING_QUEUE,
    ENTERING_QUEUE
}
