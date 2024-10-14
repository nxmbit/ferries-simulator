package com.github.nxmbit.ferriessimulator;

import javafx.scene.paint.Color;
import java.util.concurrent.locks.Lock;

public class Vehicle implements Runnable {
    private double speed;
    private final double maxSpeed;
    private Dock dock;
    private int y;
    private int x;
    private int prevX;
    private int prevY;
    private Tile[][] grid;
    private TileType[][] originalTileTypes;
    private final Color color;
    private VehicleState state;
    private VehicleState previousState;
    private VehicleState travelState;
    private long threadSleepBasedOnSpeed;
    private boolean dockChanged;

    private boolean running;

    private Tile despawnPoint;

    public Vehicle(double speed, Color color, Dock dock, Tile[][] grid, Tile spawn, Tile despawn, TileType[][] originalTileTypes) {
        this.despawnPoint = despawn;
        this.speed = this.maxSpeed = speed;
        this.color = color;
        this.dock = dock;
        this.y = this.prevY = spawn.getGridY();
        this.x = this.prevX = spawn.getGridX();
        this.grid = grid;
        this.originalTileTypes = originalTileTypes;
        grid[x][y].setType(TileType.VEHICLE);
        grid[x][y].setFill(color);
        this.state = VehicleState.GOING_STRAIGHT_UP;
        this.travelState = VehicleState.TRAVELLING_TO_DOCK;
        this.running = true;
        this.threadSleepBasedOnSpeed = (long) (1000 / speed);
        this.dockChanged = false;

    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setDock(Dock dock) {
        this.dock = dock;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (running) {
            try {
                move();
                Thread.sleep(threadSleepBasedOnSpeed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void move() throws InterruptedException {
        Lock currentLock = grid[x][y].getLock();
        currentLock.lock();
        try {

            if (isAtDespawnPoint()) {
                despawn();
                return;
            }

            if (travelState == VehicleState.TRAVELLING_TO_DOCK && isInFrontOfDockEntry()) {
                if (!enterEnteringQueue()) {
                    return; // If unable to enter dock queue, wait and retry
                }
            } else if (travelState == VehicleState.AWAITING_ON_DOCK && isInFrontOfDockTurnLeft()) {
                // If the vehicle is in front of a left turn, and there is a vehicle left of it, wait
                if (grid[x - 1][y - 1].getType() == TileType.VEHICLE) {
                    return;
                }
            } else if (travelState == VehicleState.AWAITING_ON_DOCK && isInFrontOfDockTurnRight()) {
                // If the vehicle is in front of a right turn, and there is a vehicle right of it, wait
                if (grid[x + 1][y - 1].getType() == TileType.VEHICLE) {
                    return;
                }
            } else if (travelState == VehicleState.UNLOADED_FROM_FERRY && isInFrontOfRoad()) {
                exitExitingQueue();
            } else if (travelState == VehicleState.UNLOADING_FROM_FERRY && isInCriticalSection()) {
                enterExitingQueue();
            } else if (travelState == VehicleState.AWAITING_ON_DOCK && isInFrontOfDockQueue()) {
                exitEnteringQueue();
                return; // If unable to exit dock queue, wait and retry
            } else if (travelState == VehicleState.LOADED_ON_FERRY || travelState == VehicleState.LOADING_ON_FERRY) {
                // Ferry logic will handle these states
                return;
            }

            prevX = x;
            prevY = y;

            Tile nextTile = getNextTile();
            if (nextTile != null) {
                if (nextTile.getType() == TileType.DOCK_TURN_LEFT || nextTile.getType() == TileType.DOCK_TURN_RIGHT || nextTile.getType() == TileType.DOCK_STRAIGHT_DOWN) {
                    previousState = state;
                    state = VehicleState.APPROACHING_TURN;
                }
            }

            // Adjust position based on the current state
            switch (state) {
                case APPROACHING_TURN:
                    moveInPreviousDirection(); // Move one step in the previous direction
                    if (nextTile != null) {
                        if (nextTile.getType() == TileType.DOCK_TURN_LEFT) {
                            state = VehicleState.TURNING_LEFT;
                        } else if (nextTile.getType() == TileType.DOCK_TURN_RIGHT) {
                            state = VehicleState.TURNING_RIGHT;
                        } else if (nextTile.getType() == TileType.DOCK_STRAIGHT_DOWN) {
                            state = VehicleState.GOING_STRAIGHT_DOWN;
                        }
                    }
                    break;
                case TURNING_LEFT:
                    moveHorizontal(-1);
                    break;
                case TURNING_RIGHT:
                    moveHorizontal(1);
                    break;
                case GOING_STRAIGHT_UP:
                    moveStraight(-1);
                    break;
                case GOING_STRAIGHT_DOWN:
                    moveStraight(1);
                    break;
                default:
                    break;
            }

            if (grid[x][y].getType() == TileType.VEHICLE) {
                return;
            }

            Lock newLock = grid[x][y].getLock();
            newLock.lock();
            try {
                grid[x][y].setType(TileType.VEHICLE);
                grid[x][y].setFill(color);
            } finally {
                newLock.unlock();
            }

            Lock prevLock = grid[prevX][prevY].getLock();
            prevLock.lock();
            try {
                grid[prevX][prevY].setType(originalTileTypes[prevX][prevY]);
            } finally {
                prevLock.unlock();
            }

        } finally {
            currentLock.unlock();
        }
    }

    private Tile getNextTile() {
        switch (state) {
            case TURNING_LEFT:
                return grid[x - 1][y];
            case TURNING_RIGHT:
                return grid[x + 1][y];
            case GOING_STRAIGHT_UP:
                return grid[x][y - 1];
            case GOING_STRAIGHT_DOWN:
                return grid[x][y + 1];
            default:
                return null;
        }
    }

    private boolean isAtDespawnPoint() {
        return x == despawnPoint.getGridX() && y == despawnPoint.getGridY();
    }

    private boolean isInFrontOfDockEntry() {
        Tile nextTile = getNextTile();
        return nextTile != null && nextTile.getType() == TileType.DOCK;
    }

    private boolean isInFrontOfDockQueue() {
        Tile nextTile = getNextTile();
        return nextTile != null && nextTile.getType() == TileType.DOCK_QUEUE;
    }

    private boolean isInFrontOfCriticalSection() {
        Tile nextTile = getNextTile();
        return nextTile != null && nextTile.getType() == TileType.DOCK_CRITICAL_SECTION;
    }

    private boolean isInFrontOfDockTurnLeft() {
        Tile nextTile = getNextTile();
        return nextTile != null && nextTile.getType() == TileType.DOCK_TURN_LEFT;
    }

    private boolean isInFrontOfDockTurnRight() {
        Tile nextTile = getNextTile();
        return nextTile != null && nextTile.getType() == TileType.DOCK_TURN_RIGHT;
    }

    private boolean isInFrontOfRoad() {
        Tile nextTile = getNextTile();
        return nextTile != null && nextTile.getType() == TileType.ROAD;
    }

    private boolean isInCriticalSection() {
        return originalTileTypes[x][y] == TileType.DOCK_CRITICAL_SECTION;
    }

    private boolean enterEnteringQueue() {
        if (dock.canEnterEnteringQ()) {
            dock.enterEnteringQ(this);
            travelState = VehicleState.AWAITING_ON_DOCK;
            return true;
        }
        return false;
    }

    private boolean exitEnteringQueue() throws InterruptedException {
        return enterCriticalSectionFromEnteringQueue();
    }

    private boolean enterExitingQueue() throws InterruptedException {
        changeDock();
        dock.getCriticalSectionLock().lock();
        try {
            if (dock.canEnterExitingQ()) {
                x = dock.getCriticalSectionCoordinateX();
                y = dock.getCriticalSectionCoordinateY();
                grid[x][y].setType(TileType.VEHICLE);
                grid[x][y].setFill(color);
                Thread.sleep(threadSleepBasedOnSpeed);
                dock.enterExitingQ(this);
                dock.setCriticalSectionVehicle(null);
                grid[x][y].setType(originalTileTypes[x][y]);
                x = dock.getCriticalSectionReturnCoordinateX();
                y = dock.getCriticalSectionReturnCoordinateY();
                grid[x][y].setType(TileType.VEHICLE);
                grid[x][y].setFill(color);
                travelState = VehicleState.UNLOADED_FROM_FERRY;
                return true;
            }
            return false;
        } finally {
            dock.getCriticalSectionLock().unlock();
        }
    }

    public void revertChangesToCriticalSectionWhenBoarding() {
        grid[dock.getCriticalSectionCoordinateX()][dock.getCriticalSectionCoordinateY()].setType(TileType.DOCK_CRITICAL_SECTION);
    }

    private void exitExitingQueue() {
        dock.exitExitingQ();
    }

    private void changeDock() {
        if (dockChanged == false) {
            if (dock == Simulation.getDocks().get(1)) {
                dock = Simulation.getDocks().get(2);
            } else {
                dock = Simulation.getDocks().get(1);
            }
            dockChanged = true;
        }
    }

    private boolean enterCriticalSectionFromEnteringQueue() throws InterruptedException {
        dock.getCriticalSectionLock().lock();
        try {
            while (!dock.isFerryAtDock() || grid[dock.getCriticalSectionCoordinateX()][dock.getCriticalSectionCoordinateY()].getType() != TileType.DOCK_CRITICAL_SECTION) {
                return false;
            }
            try {
                System.out.println("Vehicle " + this + " waiting for critical section");
                dock.getCriticalSectionCondition().await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }

            // Przypisz pojazd do sekcji krytycznej
            int criticalX = dock.getCriticalSectionCoordinateX();
            int criticalY = dock.getCriticalSectionCoordinateY();
            grid[x][y].setType(originalTileTypes[x][y]);
            x = criticalX;
            y = criticalY;
            grid[x][y].setType(TileType.VEHICLE);
            grid[x][y].setFill(color);
            dock.exitEnteringQ();
            dock.setCriticalSectionVehicle(this); // Zaktualizuj pojazd w sekcji krytycznej
            travelState = VehicleState.LOADING_ON_FERRY;
            Thread.sleep(threadSleepBasedOnSpeed);
            //dock.signalVehicleToEnterCriticalSection(); // Powiadom kolejny pojazd o możliwości wejścia do sekcji krytycznej
            return true;
        } finally {
            dock.getCriticalSectionLock().unlock();
        }
    }

    public VehicleState getTravelState() {
        return travelState;
    }

    public void setTravelState(VehicleState state) {
        this.travelState = state;
    }

    private void moveStraight(int direction) {
        if ((direction == -1 && y - 1 >= 0) || (direction == 1 && y + 1 < grid[0].length)) {
            Tile nextTile = grid[x][y + direction];
            Tile nextNextTile = ((direction == -1 && y - 2 >= 0) || (direction == 1 && y + 2 < grid[0].length)) ? grid[x][y + 2 * direction] : null;

            Lock nextLock = nextTile.getLock();
            nextLock.lock();
            try {
                if (nextTile.getType() == TileType.VEHICLE) {
                    speed = getAdjustedSpeed(x, y + direction);
                    return;
                } else if (nextNextTile != null && nextNextTile.getType() == TileType.VEHICLE) {
                    speed = getAdjustedSpeed(x, y + 2 * direction);
                    return;
                } else {
                    speed = maxSpeed;
                    y += direction;
                }
            } finally {
                nextLock.unlock();
            }
        } else {
            speed = maxSpeed;
            y += direction;
        }
    }

    private void moveHorizontal(int direction) {
        if ((direction == -1 && x - 1 >= 0) || (direction == 1 && x + 1 < grid.length)) {
            Tile nextTile = grid[x + direction][y];
            Tile nextNextTile = ((direction == -1 && x - 2 >= 0) || (direction == 1 && x + 2 < grid.length)) ? grid[x + 2 * direction][y] : null;

            Lock nextLock = nextTile.getLock();
            nextLock.lock();
            try {
                if (nextTile.getType() == TileType.VEHICLE) {
                    speed = getAdjustedSpeed(x + direction, y);
                    return;
                } else if (nextNextTile != null && nextNextTile.getType() == TileType.VEHICLE) {
                    speed = getAdjustedSpeed(x + 2 * direction, y);
                    return;
                } else {
                    speed = maxSpeed;
                    x += direction;
                }
            } finally {
                nextLock.unlock();
            }
        } else {
            speed = maxSpeed;
            x += direction;
        }
    }

    private void moveInPreviousDirection() {
        switch (previousState) {
            case GOING_STRAIGHT_UP:
                moveStraight(-1);
                break;
            case GOING_STRAIGHT_DOWN:
                moveStraight(1);
                break;
            case TURNING_LEFT:
                moveHorizontal(-1);
                break;
            case TURNING_RIGHT:
                moveHorizontal(1);
                break;
            default:
                break;
        }
    }

    public Tile getTile() {
        return grid[x][y];
    }

    public long unloadVehiclesWaittime() {
        return threadSleepBasedOnSpeed;
    }

    private double getAdjustedSpeed(int x, int y) {
        Vehicle vehicleAhead = getVehicleAhead(x, y);
        if (vehicleAhead != null) {
            return Math.min(speed, vehicleAhead.speed);
        }
        return speed;
    }

    private Vehicle getVehicleAhead(int x, int y) {
        synchronized (Simulation.getVehicles()) {
            for (Vehicle vehicle : Simulation.getVehicles()) {
                if (vehicle.getX() == x && vehicle.getY() == y) {
                    return vehicle;
                }
            }
        }
        return null;
    }

    private void despawn() {
        System.out.println("Vehicle despawned at (" + x + ", " + y + ")");
        Lock lock = grid[x][y].getLock();
        lock.lock();
        try {
            grid[x][y].setType(originalTileTypes[x][y]);
        } finally {
            lock.unlock();
        }
        Simulation.removeVehicle(this);
        running = false; // Stop the vehicle's thread
    }
}
