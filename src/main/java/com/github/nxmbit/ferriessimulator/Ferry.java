package com.github.nxmbit.ferriessimulator;

import java.util.ArrayList;
import java.util.List;

public class Ferry implements Runnable {
    private double speed;
    private int capacity;
    private double x;
    private double y;
    private Dock currentDock;
    private Dock targetDock;
    private FerryState state;
    private List<Vehicle> vehicles = new ArrayList<>();

    public Ferry(double speed, int capacity, double x, double y, Dock currentDock, Dock targetDock) {
        this.speed = speed;
        this.capacity = capacity;
        this.x = x;
        this.y = y;
        this.currentDock = currentDock;
        this.targetDock = targetDock;
        this.state = FerryState.LOADING;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
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
            state = FerryState.LOADING;
            loadVehicles();
        } else if (state == FerryState.LOADING && vehicles.size() == capacity) {
            state = FerryState.TRAVELING;
            Dock temp = currentDock;
            currentDock = targetDock;
            targetDock = temp;
        }

        if (state == FerryState.TRAVELING) {
            x += speed;
        }
    }

    public void loadVehicles() {
        // Synchronize on the vehicles list to prevent multiple threads from modifying it at the same time
        synchronized (vehicles) {
            while (vehicles.size() < capacity && currentDock.hasVehicles()) {
                vehicles.add(currentDock.exit());
            }
        }
    }

    public boolean isAtDock(Dock dock) {
//        // Sprawdź, czy prom jest przy określonym doku
//        // Możesz dostosować te wartości, aby lepiej pasowały do twojej symulacji
//        double dockStartX = dock.getX();
//        double dockEndX = dock.getX() + dock.getWidth();
//        double dockStartY = dock.getY();
//        double dockEndY = dock.getY() + dock.getHeight();
//
//        return x >= dockStartX && x <= dockEndX && y >= dockStartY && y <= dockEndY;
        return true;
    }

}

enum FerryState {
    LOADING,
    TRAVELING
}