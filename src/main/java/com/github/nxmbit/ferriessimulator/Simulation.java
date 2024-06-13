package com.github.nxmbit.ferriessimulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;

public class Simulation implements Runnable {
    private static final List<Vehicle> vehicles = Collections.synchronizedList(new ArrayList<>());
    private List<Ferry> ferries;
    private VehicleSpawner vehicleSpawner;
    private int maxCarsCount;

    private MapImport mapImport;

    private Dock leftDock;
    private Dock rightDock;

    private Map<Integer, Tile> spawnPoints;
    private Map<Integer, Tile> despawnPoints;
    private static Map<Integer, Dock> docks;


    public Simulation() {
        this.ferries = new ArrayList<>();
        this.spawnPoints = new HashMap<>();
        this.despawnPoints = new HashMap<>();
        this.docks = new HashMap<>();
        this.maxCarsCount = 5;
        this.mapImport = new MapImport();
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

    public void setup(int ferryCount, int dockHeight, double tileSize, int maxLoadingTime, Tile[][] grid, TileType[][] originalTileTypes) {
        setSpawnAndDespawnPoints(grid);

        leftDock = new Dock(mapImport.getDock1EnteringCapacity(), mapImport.getDock1ExitingCapacity(),
                mapImport.getDock1FerryCoordinateX(), mapImport.getDock1FerryCoordinateY(),
                mapImport.getDock1CriticalSectionCoordinateX(), mapImport.getDock1CriticalSectionCoordinateY(),
                mapImport.getDock1CriticalSectionReturnCoordinateX(), mapImport.getDock1CriticalSectionReturnCoordinateY());
        rightDock = new Dock(mapImport.getDock2EnteringCapacity(), mapImport.getDock2ExitingCapacity(),
                mapImport.getDock2FerryCoordinateX(), mapImport.getDock2FerryCoordinateY(),
                mapImport.getDock2CriticalSectionCoordinateX(), mapImport.getDock2CriticalSectionCoordinateY(),
                mapImport.getDock2CriticalSectionReturnCoordinateX(), mapImport.getDock2CriticalSectionReturnCoordinateY());
        docks.put(1, leftDock);
        docks.put(2, rightDock);

        System.out.println("koordynaty docku: " + leftDock.getFerryCoordinateX() + " " + leftDock.getFerryCoordinateY() + " " + tileSize);

        ferries.clear();
        for (int i = 0; i < ferryCount; i++) {
            int capacity = 2 + new Random().nextInt(5) * 2; // Random capacity between 2 and 10
            Ferry ferry = new Ferry(8.0, capacity, leftDock, rightDock, maxLoadingTime, tileSize, dockHeight);
            ferries.add(ferry);
        }

        vehicleSpawner = new VehicleSpawner(docks, grid, maxCarsCount, spawnPoints, despawnPoints, originalTileTypes, 5000, vehicles);
    }

    public List<Ferry> getFerries() {
        return ferries;
    }

    public static List<Vehicle> getVehicles() {
        return vehicles;
    }

    public static Map<Integer, Dock> getDocks() {
        return docks;
    }

    public VehicleSpawner getVehicleSpawner() {
        return vehicleSpawner;
    }

    private void spawnFerries() {
        for (Ferry ferry : ferries) {
            new Thread(ferry).start();
        }
    }

    @Override
    public void run() {
        spawnFerries();
        vehicleSpawner.startSpawning();
        while (true) {
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

    public static void removeVehicle(Vehicle vehicle) {
        synchronized (vehicles) {
            vehicles.remove(vehicle);
            System.out.println("Vehicle removed. Current vehicle count: " + vehicles.size());
        }
    }
}
