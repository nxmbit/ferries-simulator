package com.github.nxmbit.ferriessimulator;//package com.github.nxmbit.ferriessimulator;
//
//import java.util.concurrent.Semaphore;
//import java.util.ArrayList;
//import java.util.List;
//
//public class Dock {
//    private final Semaphore semaphore;
//    private final List<Vehicle> vehicles;
//
//    public Dock(int capacity) {
//        this.semaphore = new Semaphore(capacity);
//        this.vehicles = new ArrayList<>();
//    }
//
//    public boolean canEnter() {
//        return semaphore.availablePermits() > 0;
//    }
//
//    public void enter(Vehicle vehicle) {
//        try {
//            semaphore.acquire();
//            vehicles.add(vehicle);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public Vehicle exit() {
//        if (hasVehicles()) {
//            semaphore.release();
//            return vehicles.remove(0);
//        } else {
//            return null;
//        }
//    }
//
//    public boolean hasVehicles() {
//        return !vehicles.isEmpty();
//    }
//
//    public void reset() {
//        semaphore.release(semaphore.availablePermits());
//        vehicles.clear();
//    }
//}

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Dock {
    private final Semaphore enteringSemaphore;
    private final Semaphore exitingSemaphore;
    private final Lock criticalSectionLock;
    private final LinkedBlockingQueue<Vehicle> enteringQueue;
    private final LinkedBlockingQueue<Vehicle> exitingQueue;

    public Dock(int enteringCapacity, int exitingCapacity) {
        this.enteringSemaphore = new Semaphore(enteringCapacity);
        this.exitingSemaphore = new Semaphore(exitingCapacity);
        this.criticalSectionLock = new ReentrantLock();
        this.enteringQueue = new LinkedBlockingQueue<>();
        this.exitingQueue = new LinkedBlockingQueue<>();
    }

    public boolean canEnter() {
        return enteringSemaphore.availablePermits() > 0;
    }

    public boolean canExit() {
        return exitingSemaphore.availablePermits() > 0;
    }

    public void enter(Vehicle vehicle) {
        try {
            enteringSemaphore.acquire();
            enteringQueue.add(vehicle);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void exit(Vehicle vehicle) {
        try {
            exitingSemaphore.acquire();
            exitingQueue.add(vehicle);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Vehicle removeEnteringVehicle() {
        enteringSemaphore.release();
        return enteringQueue.poll();
    }

    public Vehicle removeExitingVehicle() {
        exitingSemaphore.release();
        return exitingQueue.poll();
    }

    public boolean hasQueuedEnteringVehicles() {
        return !enteringQueue.isEmpty();
    }

    public boolean hasQueuedExitingVehicles() {
        return !exitingQueue.isEmpty();
    }

    public Lock getCriticalSectionLock() {
        return criticalSectionLock;
    }

    public void reset() {
        enteringSemaphore.release(enteringSemaphore.availablePermits());
        exitingSemaphore.release(exitingSemaphore.availablePermits());
        enteringQueue.clear();
        exitingQueue.clear();
    }
}
