package com.github.nxmbit.ferriessimulator;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Ferry implements Runnable {
    private double speed;
    private int capacity;
    private double x, y;
    private Dock currentDock;
    private Dock targetDock;
    private FerryState state;
    private ConcurrentLinkedQueue<Vehicle> vehicles;

    public Ferry(double speed, int capacity, double x, double y, Dock currentDock, Dock targetDock) {
        this.speed = speed;
        this.capacity = capacity;
        this.x = x;
        this.y = y;
        this.currentDock = currentDock;
        this.targetDock = targetDock;
        this.state = FerryState.LOADING;
        this.vehicles = new ConcurrentLinkedQueue<>();
    }

    public void run() {
        while (true) {
            move();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void move() {
        if (state == FerryState.TRAVELING && isAtDock(targetDock)) {
            state = FerryState.UNLOADING;
            unloadVehicles();
        } else if (state == FerryState.UNLOADING && vehicles.isEmpty()) {
            state = FerryState.LOADING;
            loadVehicles();
        }

        if (state == FerryState.TRAVELING) {
            x += speed;
        }
    }

    public void loadVehicles() {
        synchronized (currentDock.getCriticalSectionLock()) {
            while (vehicles.size() < capacity && currentDock.hasQueuedEnteringVehicles()) {
                vehicles.add(currentDock.dequeueEnteringVehicle());
            }
        }
    }

    public void unloadVehicles() {
        synchronized (targetDock.getCriticalSectionLock()) {
            while (!vehicles.isEmpty()) {
                Vehicle vehicle = vehicles.poll();
                targetDock.exit(vehicle);
            }
        }
    }

    public boolean isAtDock(Dock dock) {
        // Implement logic to check if the ferry is at the dock
        return true;
    }
}

enum FerryState {
    LOADING,
    UNLOADING,
    TRAVELING
}