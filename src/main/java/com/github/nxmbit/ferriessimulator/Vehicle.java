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
    private TileType originalTileType;
    private boolean justSpawned;
    private boolean wasWaiting;
    private final Color color;

    private Tile spawnPoint;
    private Tile despawnPoint;

    public Vehicle(double speed, Color color, Dock dock, Tile[][] grid, Tile spawn, Tile despawn) {
        this.spawnPoint = spawn;
        this.despawnPoint = despawn;
        this.speed = this.maxSpeed = speed; // Prędkość jest ustawiana na podstawie losowości
        this.color = color; // Ustawiamy kolor pojazdu
        this.dock = dock;
        this.y = this.prevY = spawn.getGridY();
        this.x = this.prevX = spawn.getGridX();
        this.grid = grid;
        this.originalTileType = grid[x][y].getType();
        grid[x][y].setType(TileType.VEHICLE);
        grid[x][y].setFill(color); // Ustawiamy kolor od razu po spawnowaniu
        this.justSpawned = true;
        this.wasWaiting = false;

        // Logowanie przy spawnowaniu pojazdu
        System.out.println("Vehicle spawned at (" + x + ", " + y + ") with speed " + speed + " and color " + color);
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

        while (true) {
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
            if (justSpawned) {
                justSpawned = false;
            } else {
                originalTileType = grid[prevX][prevY].getType();
            }

            prevX = x;
            prevY = y;

            if (wasWaiting) {
                wasWaiting = false;
                originalTileType = TileType.ROAD;
            }

            if (y - 1 >= 0) {
                Tile nextTile = grid[x][y - 1];
                Tile nextNextTile = (y - 2 >= 0) ? grid[x][y - 2] : null;

                Lock nextLock = nextTile.getLock();
                nextLock.lock();
                try {
                    if (nextNextTile != null && nextNextTile.getType() == TileType.VEHICLE) {
                        System.out.println("block 1 - nextNextTile is VEHICLE");
                        speed = getAdjustedSpeed(x, y - 2);
                        wasWaiting = true;
                        System.out.println("Vehicle at (" + x + ", " + y + ") adjusted speed to " + speed + " due to vehicle ahead.");
                        return; // Czekaj, aż pole przed pojazdem będzie wolne
                    } else {
                        System.out.println("block 2");
                        speed = maxSpeed;
                        y--; // Przesuwamy się do przodu
                        // Logowanie ruchu pojazdu
                        System.out.println("Vehicle at (" + x + ", " + y + ") moving forward to (" + x + ", " + (y - 1) + ") with speed " + speed);
                    }
                } finally {
                    System.out.println("block 3");
                    nextLock.unlock();
                }
            } else {
                System.out.println("block 4");
                speed = maxSpeed;
                y--; // Przesuwamy się do przodu
                // Logowanie ruchu pojazdu
                System.out.println("Vehicle at (" + x + ", " + y + ") moving forward to (" + x + ", " + (y - 1) + ") with speed " + speed);
            }

            // Sprawdzanie, czy nie wychodzimy poza granice tablicy
            if (y < 0 || y >= grid[0].length) {
                System.out.println("Vehicle at (" + x + ", " + y + ") attempted to move out of bounds.");
                return;
            }

            if (grid[x][y].getType() == TileType.VEHICLE) {
                System.out.println("Vehicle at (" + x + ", " + y + ") attempted to move to an occupied tile.");
                return; // Nie wykonujemy ruchu, jeśli kafelek jest zajęty
            }

            Lock newLock = grid[x][y].getLock();
            newLock.lock();
            try {
                grid[x][y].setType(TileType.VEHICLE);
                grid[x][y].setFill(color); // Ustawiamy kolor pojazdu
            } finally {
                newLock.unlock();
            }

            Lock prevLock = grid[prevX][prevY].getLock();
            prevLock.lock();
            try {
                grid[prevX][prevY].setType(originalTileType);
            } finally {
                prevLock.unlock();
            }
        } finally {
            currentLock.unlock();
        }
    }

    private double getAdjustedSpeed(int x, int y) {
        Vehicle vehicleAhead = getVehicleAhead(x, y);
        if (vehicleAhead != null) {
            return Math.min(speed, vehicleAhead.speed); // Dostosuj prędkość do pojazdu przed nim
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
}
