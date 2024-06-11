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
    private boolean justSpawned;
    private boolean wasWaiting;
    private final Color color;
    private VehicleState state;
    private VehicleState previousState;

    private boolean running;

    private Tile spawnPoint;
    private Tile despawnPoint;

    public Vehicle(double speed, Color color, Dock dock, Tile[][] grid, Tile spawn, Tile despawn, TileType[][] originalTileTypes) {
        this.spawnPoint = spawn;
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
        this.justSpawned = true;
        this.wasWaiting = false;
        this.state = VehicleState.GOING_STRAIGHT_UP;
        this.running = true;

        //System.out.println("Vehicle spawned at (" + x + ", " + y + ") with speed " + speed + " and color " + color);
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
                Thread.sleep((long) (1000 / speed));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void move() {
        Lock currentLock = grid[x][y].getLock();
        currentLock.lock();
        try {

            prevX = x;
            prevY = y;

            if (isAtDespawnPoint()) {
                despawn();
                return;
            }

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
                case TRAVELLING_TO_DOCK:
                case AWAITING_ON_DOCK:
                case LOADED_ON_FERRY:
                case UNLOADING_FROM_FERRY:
                case TRAVELLING_FROM_DOCK:
                    // Handle other states as needed
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
                grid[prevX][prevY].setType(originalTileTypes[prevX][prevY]); // Przywracanie oryginalnego typu kafelka
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
                    wasWaiting = true;
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
                    wasWaiting = true;
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
