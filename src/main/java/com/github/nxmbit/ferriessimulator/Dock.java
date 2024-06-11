package com.github.nxmbit.ferriessimulator;

import java.util.concurrent.Semaphore;
import java.util.ArrayList;
import java.util.List;

public class Dock {
    private final Semaphore semaphore;
    private final List<Vehicle> vehicles;

    public Dock(int capacity) {
        this.semaphore = new Semaphore(capacity);
        this.vehicles = new ArrayList<>();
    }

    public boolean canEnter() {
        return semaphore.availablePermits() > 0;
    }

    public void enter(Vehicle vehicle) {
        try {
            semaphore.acquire();
            vehicles.add(vehicle);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Vehicle exit() {
        if (hasVehicles()) {
            semaphore.release();
            return vehicles.remove(0);
        } else {
            return null;
        }
    }

    public boolean hasVehicles() {
        return !vehicles.isEmpty();
    }

    public void reset() {
        semaphore.release(semaphore.availablePermits());
        vehicles.clear();
    }
}