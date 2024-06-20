package com.github.nxmbit.ferriessimulator;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;

public class SettingsImport {
    private int maxVehicles;
    private long vehiclesSpawnInterval;
    private int minRandomFerryCapacity;
    private int maxRandomFerryCapacity;
    private int minRandomFerryLoadingTime;
    private int maxRandomFerryLoadingTime;
    private double ferrySpeed;
    private double minRandomVehicleSpeed;
    private double maxRandomVehicleSpeed;
    private double leftRightDockSpawnBalance;

    public SettingsImport() {
        try (InputStream is = getClass().getResourceAsStream("/com/github/nxmbit/ferriessimulator/settings.json")) {
            if (is == null) {
                throw new IllegalArgumentException("Settings file not found.");
            }
            JSONObject obj = new JSONObject(new JSONTokener(is));
            this.maxVehicles = obj.getInt("maxVehicles");
            this.vehiclesSpawnInterval = obj.getLong("vehiclesSpawnInterval");
            this.minRandomFerryCapacity = obj.getInt("minRandomFerryCapacity");
            this.maxRandomFerryCapacity = obj.getInt("maxRandomFerryCapacity");
            this.minRandomFerryLoadingTime = obj.getInt("minRandomFerryLoadingTime");
            this.maxRandomFerryLoadingTime = obj.getInt("maxRandomFerryLoadingTime");
            this.ferrySpeed = obj.getDouble("ferrySpeed");
            this.minRandomVehicleSpeed = obj.getDouble("minRandomVehicleSpeed");
            this.maxRandomVehicleSpeed = obj.getDouble("maxRandomVehicleSpeed");
            this.leftRightDockSpawnBalance = obj.getDouble("leftRightDockSpawnBalance");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load settings from file. ", e);
        }

    }

    public int getMaxVehicles() {
        return maxVehicles;
    }

    public long getVehiclesSpawnInterval() {
        return vehiclesSpawnInterval;
    }

    public int getMinRandomFerryCapacity() {
        return minRandomFerryCapacity;
    }

    public int getMaxRandomFerryCapacity() {
        return maxRandomFerryCapacity;
    }

    public int getMinRandomFerryLoadingTime() {
        return minRandomFerryLoadingTime;
    }

    public int getMaxRandomFerryLoadingTime() {
        return maxRandomFerryLoadingTime;
    }

    public double getFerrySpeed() {
        return ferrySpeed;
    }

    public double getMinRandomVehicleSpeed() {
        return minRandomVehicleSpeed;
    }

    public double getMaxRandomVehicleSpeed() {
        return maxRandomVehicleSpeed;
    }

    public double getLeftRightDockSpawnBalance() {
        return leftRightDockSpawnBalance;
    }
}
