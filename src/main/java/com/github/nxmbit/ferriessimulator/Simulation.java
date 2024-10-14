package com.github.nxmbit.ferriessimulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;

public class Simulation implements Runnable {
    private static final List<Vehicle> vehicles = Collections.synchronizedList(new ArrayList<>());
    private final List<Ferry> ferries;
    private VehicleSpawner vehicleSpawner;

    private MapImport mapImport;
    private final SettingsImport settings;

    private Dock leftDock;
    private Dock rightDock;

    private Map<Integer, Tile> spawnPoints;
    private Map<Integer, Tile> despawnPoints;
    private static Map<Integer, Dock> docks;

    private boolean simulationRunning;


    public Simulation() {
        this.ferries = Collections.synchronizedList(new ArrayList<>());
        this.spawnPoints = new HashMap<>();
        this.despawnPoints = new HashMap<>();
        this.docks = new HashMap<>();
        this.mapImport = new MapImport();
        this.settings = new SettingsImport();
        this.simulationRunning = true;
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

    public void setup(int dockHeight, double tileSize, Tile[][] grid, TileType[][] originalTileTypes,
                      int dock1EnteringCapacity, int dock1ExitingCapacity, int dock2EnteringCapacity, int dock2ExitingCapacity,
                      double ferrySpeed, int dock1FerriesSpawned, int dock2FerriesSpawned, int minLoadingTime, int maxLoadingTime,
                      int minFerryCapacity, int maxFerryCapacity) {
        setSpawnAndDespawnPoints(grid);

        leftDock = new Dock(dock1EnteringCapacity, dock1ExitingCapacity,
                mapImport.getDock1FerryCoordinateX(), mapImport.getDock1FerryCoordinateY(),
                mapImport.getDock1CriticalSectionCoordinateX(), mapImport.getDock1CriticalSectionCoordinateY(),
                mapImport.getDock1CriticalSectionReturnCoordinateX(), mapImport.getDock1CriticalSectionReturnCoordinateY(),
                mapImport.getDock1FerryQueueSize(), mapImport.getDock1FerryQueueCoordinateX(), mapImport.getDock1FerryQueueCoordinateY(),
                mapImport.getDock1ToDock2LaneCoordinateX(), mapImport.getDock1ToDock2LaneCoordinateY(),
                mapImport.getDock1ToDock2LaneCoordinateXEnd(), mapImport.getDock1ToDock2LaneCoordinateYEnd(), mapImport.getDock1ToDock2GoDownToQueueCoordinateX());
        rightDock = new Dock(dock2EnteringCapacity, dock2ExitingCapacity,
                mapImport.getDock2FerryCoordinateX(), mapImport.getDock2FerryCoordinateY(),
                mapImport.getDock2CriticalSectionCoordinateX(), mapImport.getDock2CriticalSectionCoordinateY(),
                mapImport.getDock2CriticalSectionReturnCoordinateX(), mapImport.getDock2CriticalSectionReturnCoordinateY(),
                mapImport.getDock2FerryQueueSize(), mapImport.getDock2FerryQueueCoordinateX(), mapImport.getDock2FerryQueueCoordinateY(),
                mapImport.getDock2ToDock1LaneCoordinateX(), mapImport.getDock2ToDock1LaneCoordinateY(),
                mapImport.getDock2ToDock1LaneCoordinateXEnd(), mapImport.getDock2ToDock1LaneCoordinateYEnd(), mapImport.getDock2ToDock1GoDownToQueueCoordinateX());

        docks.put(1, leftDock);
        docks.put(2, rightDock);

        ferries.clear();
        int totalFerries = 0;
        int maxFerries = mapImport.getMaxNumberOfFerries();

        for (int i = 0; i < dock1FerriesSpawned && totalFerries < maxFerries; i++, totalFerries++) {
            int capacity = minFerryCapacity + new Random().nextInt(maxFerryCapacity);
            int loadingTime = (minLoadingTime + new Random().nextInt(maxLoadingTime)) * 1000;
            Ferry ferry = new Ferry(ferrySpeed, capacity, leftDock, rightDock, loadingTime, tileSize, dockHeight);
            leftDock.addFerryToQueueOnSpawn(ferry);
            ferry.setQueuePosition(i);
            ferries.add(ferry);
        }

        for (int i = 0; i < dock2FerriesSpawned && totalFerries < maxFerries; i++, totalFerries++) {
            int capacity = minFerryCapacity + new Random().nextInt(maxFerryCapacity);
            int loadingTime = (minLoadingTime + new Random().nextInt(maxLoadingTime)) * 1000;
            Ferry ferry = new Ferry(ferrySpeed, capacity, rightDock, leftDock, loadingTime, tileSize, dockHeight);
            rightDock.addFerryToQueueOnSpawn(ferry);
            ferry.setQueuePosition(i);
            ferries.add(ferry);
        }

        vehicleSpawner = new VehicleSpawner(docks, grid, settings.getMaxVehicles(), spawnPoints, despawnPoints, originalTileTypes,
                settings.getVehiclesSpawnInterval(), vehicles, settings.getLeftRightDockSpawnBalance(),
                settings.getMinRandomVehicleSpeed(), settings.getMaxRandomVehicleSpeed());
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
        while (simulationRunning) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (vehicleSpawner != null) {
            vehicleSpawner.stopAllVehiclesAndSpawning();
        }

        synchronized (ferries) {
            for (Ferry ferry : ferries) {
                ferry.stop();
            }

            ferries.clear();
        }

        simulationRunning = false;
    }

    public static void removeVehicle(Vehicle vehicle) {
        synchronized (vehicles) {
            vehicles.remove(vehicle);
            System.out.println("Vehicle removed. Current vehicle count: " + vehicles.size());
        }
    }
}
