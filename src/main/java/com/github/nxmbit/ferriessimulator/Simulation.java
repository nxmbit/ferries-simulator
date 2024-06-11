package com.github.nxmbit.ferriessimulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Simulation implements Runnable {
    private static final List<Vehicle> vehicles = Collections.synchronizedList(new ArrayList<>());
    private List<Ferry> ferries;
    private VehicleSpawner vehicleSpawner;
    private int maxCarsCount;

    private Dock leftDock;
    private Dock rightDock;

    private Map<Integer, Tile> spawnPoints;
    private Map<Integer, Tile> despawnPoints;
    private Map<Integer, Dock> docks;

    private double canvasWidth;
    private double canvasHeight;
    private double roadWidth;
    private double dockWidth;
    private double dockHeight;

    public Simulation() {
        this.ferries = new ArrayList<>();
        this.spawnPoints = new HashMap<>();
        this.despawnPoints = new HashMap<>();
        this.docks = new HashMap<>();
        this.maxCarsCount = 5;
    }

    private void setSpawnAndDespawnPoints(Tile[][] grid) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                Tile tile = grid[i][j];
                if (tile.getType() == TileType.ROAD_SPAWN_DOCK_1) {
                    spawnPoints.put(1, tile);
                } else if (tile.getType() == TileType.ROAD_SPAWN_DOCK_2) {
                    spawnPoints.put(2, tile);
                } else if (tile.getType() == TileType.ROAD_QUIT_DOCK_1) {
                    despawnPoints.put(1, tile);
                } else if (tile.getType() == TileType.ROAD_QUIT_DOCK_2) {
                    despawnPoints.put(2, tile);
                }
            }
        }
    }

    public Map<Integer, Tile> getSpawnPoints() {
        return spawnPoints;
    }

    public Map<Integer, Tile> getDespawnPoints() {
        return despawnPoints;
    }

    private void setDocks() {
        docks.put(1, leftDock);
        docks.put(2, rightDock);
    }

    public void setup(int ferryCount, int capacity, double canvasWidth, double canvasHeight, double roadWidth, double dockWidth, double dockHeight, int maxLoadingTime, Tile[][] grid, TileType[][] originalTileTypes) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.roadWidth = roadWidth;
        this.dockWidth = dockWidth;
        this.dockHeight = dockHeight;

        setSpawnAndDespawnPoints(grid);

        leftDock = new Dock(capacity);
        rightDock = new Dock(capacity);
        setDocks();

        ferries.clear();
        for (int i = 0; i < ferryCount; i++) {
            Ferry ferry = new Ferry(1.0, capacity, canvasWidth / (ferryCount + 1) * (i + 1), canvasHeight / 2, leftDock, rightDock);  // Example starting positions
            ferries.add(ferry);
            new Thread(ferry).start();
        }

        vehicleSpawner = new VehicleSpawner(docks, roadWidth, canvasHeight, grid, maxCarsCount, spawnPoints, despawnPoints, originalTileTypes, 5000);
    }

    public List<Ferry> getFerries() {
        return ferries;
    }

    public static List<Vehicle> getVehicles() {
        return vehicles;
    }

    public VehicleSpawner getVehicleSpawner() {
        return vehicleSpawner;
    }

    @Override
    public void run() {
        vehicleSpawner.startSpawning();
        while (true) {
            update();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (vehicleSpawner != null) {
            vehicleSpawner.stopSpawning();
        }
    }

    public void update() {
        for (Ferry ferry : ferries) {
            ferry.move();
        }
    }

    public static void removeVehicle(Vehicle vehicle) {
        synchronized (vehicles) {
            vehicles.remove(vehicle);
        }
    }
}
