package com.github.nxmbit.ferriessimulator;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Dock {
    private final Semaphore enteringSemaphore;
    private final Semaphore exitingSemaphore;
    private final Lock criticalSectionLock;
    private final ConcurrentLinkedQueue<Vehicle> enteringQueue;
    private final ConcurrentLinkedQueue<Vehicle> exitingQueue;

    public Dock(int enteringCapacity, int exitingCapacity) {
        this.enteringSemaphore = new Semaphore(enteringCapacity);
        this.exitingSemaphore = new Semaphore(exitingCapacity);
        this.criticalSectionLock = new ReentrantLock();
        this.enteringQueue = new ConcurrentLinkedQueue<>();
        this.exitingQueue = new ConcurrentLinkedQueue<>();
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

    public Vehicle dequeueEnteringVehicle() {
        enteringSemaphore.release();
        return enteringQueue.poll();
    }

    public Vehicle dequeueExitingVehicle() {
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
