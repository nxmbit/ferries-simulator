package com.github.nxmbit.ferriessimulator;

import javafx.scene.paint.Color;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VehicleSpawner {
    private Tile[][] grid;
    private TileType[][] originalTileTypes;
    private ScheduledExecutorService executorService;
    private List<Vehicle> vehicles;
    private int maxCars;
    private long spawnInterval;
    private Map<Integer, Tile> spawnPoints;
    private Map<Integer, Tile> despawnPoints;
    private Map<Integer, Dock> docks;
    private final Random random = new Random();
    private final double minSpeed = 4.0; // Minimalna prędkość pojazdu
    private final double maxSpeed = 8.0; // Maksymalna prędkość pojazdu
    private final Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.PURPLE}; // Jaskrawe kolory

    public VehicleSpawner(Map<Integer, Dock> docks, Tile[][] grid, int maxCars, Map<Integer, Tile> spawnPoints, Map<Integer, Tile> despawnPoints, TileType[][] originalTileTypes, long spawnInterval, List<Vehicle> vehicles) {
        this.docks = docks;
        this.grid = grid;
        this.originalTileTypes = originalTileTypes;
        this.maxCars = maxCars;
        this.spawnInterval = spawnInterval;
        this.vehicles = vehicles;
        this.spawnPoints = spawnPoints;
        this.despawnPoints = despawnPoints;
        this.executorService = Executors.newScheduledThreadPool(1);  // Use single-threaded executor for spawning
    }

    public void startSpawning() {
        executorService.scheduleAtFixedRate(this::trySpawnVehicle, 0, spawnInterval, TimeUnit.MILLISECONDS);
    }

    public void trySpawnVehicle() {
        synchronized (vehicles) {
            System.out.println("Trying to spawn vehicle. Current vehicle count: " + vehicles.size() + ", Max cars: " + maxCars);
            if (vehicles.size() < maxCars) {
                int spawnPointId = random.nextInt(2) + 1; // randomly select dock 1 or dock 2
                attemptSpawnVehicle(spawnPointId);
            }
        }
    }

    public void attemptSpawnVehicle(int spawnPointId) {
        Tile spawnPoint = spawnPoints.get(spawnPointId);
        Tile despawnPoint = despawnPoints.get((spawnPointId == 1) ? 2 : 1);
        Dock dock = docks.get(spawnPointId);

        if (spawnPoint == null || despawnPoint == null || dock == null) {
            return;
        }

        // Synchronize on the spawn point to avoid concurrent spawns at the same location
        synchronized (spawnPoint) {
            // Check if the spawn point is free
            if (spawnPoint.getType() == TileType.VEHICLE) {
                return;
            }

            // Check if the tile in front of the spawn point is free
            int frontY = spawnPoint.getGridY() - 1;
            if (frontY >= 0 && grid[spawnPoint.getGridX()][frontY].getType() == TileType.VEHICLE) {
                return; // Do not spawn if the space is occupied
            }

            // Generate a random speed and color for the vehicle
            double speed = minSpeed + (maxSpeed - minSpeed) * random.nextDouble();
            Color color = colors[random.nextInt(colors.length)];

            // Spawn the vehicle if the space is free
            Vehicle vehicle = new Vehicle(speed, color, dock, grid, spawnPoint, despawnPoint, originalTileTypes);
            synchronized (vehicles) {
                vehicles.add(vehicle);
            }

            spawnPoint.setType(TileType.VEHICLE);
            spawnPoint.setFill(color);

            // Logowanie spawnowania pojazdu
            System.out.println("Vehicle spawned at (" + spawnPoint.getGridX() + ", " + spawnPoint.getGridY() + ") with speed " + speed + " and color " + color);

            new Thread(vehicle).start();
        }
    }

    public void stopSpawning() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setSpawnInterval(long interval) {
        this.spawnInterval = interval;
        restartSpawning();
    }

    public void setMaxCars(int maxCars) {
        this.maxCars = maxCars;
    }

    private void restartSpawning() {
        stopSpawning();
        executorService = Executors.newScheduledThreadPool(4);
        startSpawning();
    }

}
