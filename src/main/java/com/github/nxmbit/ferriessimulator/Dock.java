package com.github.nxmbit.ferriessimulator;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Dock {
    private final Semaphore enteringSemaphore;
    private final Semaphore exitingSemaphore;
    private final Lock criticalSectionLock;
    private final Lock dockLock;
    private final Condition criticalSectionCondition;
    private final ConcurrentLinkedQueue<Vehicle> enteringQueue;
    private final ConcurrentLinkedQueue<Vehicle> exitingQueue;
    private final ConcurrentLinkedQueue<Ferry> ferryQueue;
    private final double ferryCoordinateX;
    private final double ferryCoordinateY;
    private final int criticalSectionCoordinateX;
    private final int criticalSectionCoordinateY;
    private Vehicle criticalSectionVehicle;
    private final AtomicBoolean isFerryAtDock;
    private int criticalSectionReturnCoordinateX;
    private int criticalSectionReturnCoordinateY;
    private final int ferryQueueSize;
    private final int ferryQueueCoordinateX;
    private final int ferryQueueCoordinateY;

    private final int laneToNextDockStartX;
    private final int laneToNextDockStartY;
    private final int laneToNextDockEndX;
    private final int laneToNextDockEndY;


    public Dock(int enteringCapacity, int exitingCapacity, double ferryCoordinateX, double ferryCoordinateY, int criticalSectionCoordinateX,
                int criticalSectionCoordinateY, int criticalSectionReturnCoordinateX, int criticalSectionReturnCoordinateY,
                int ferryQueueSize, int ferryQueueCoordinateX, int ferryQueueCoordinateY, int laneToNextDockStartX, int laneToNextDockStartY,
                int laneToNextDockEndX, int laneToNextDockEndY) {
        this.enteringSemaphore = new Semaphore(enteringCapacity);
        this.exitingSemaphore = new Semaphore(exitingCapacity);
        this.criticalSectionLock = new ReentrantLock();
        this.dockLock = new ReentrantLock();
        this.criticalSectionCondition = criticalSectionLock.newCondition();
        this.enteringQueue = new ConcurrentLinkedQueue<>();
        this.exitingQueue = new ConcurrentLinkedQueue<>();
        this.ferryQueue = new ConcurrentLinkedQueue<>();
        this.ferryCoordinateX = ferryCoordinateX;
        this.ferryCoordinateY = ferryCoordinateY;
        this.criticalSectionCoordinateX = criticalSectionCoordinateX;
        this.criticalSectionCoordinateY = criticalSectionCoordinateY;
        this.criticalSectionVehicle = null;
        this.isFerryAtDock = new AtomicBoolean(false);
        this.criticalSectionReturnCoordinateX = criticalSectionReturnCoordinateX;
        this.criticalSectionReturnCoordinateY = criticalSectionReturnCoordinateY;
        this.ferryQueueSize = ferryQueueSize;
        this.ferryQueueCoordinateX = ferryQueueCoordinateX;
        this.ferryQueueCoordinateY = ferryQueueCoordinateY;
        this.laneToNextDockStartX = laneToNextDockStartX;
        this.laneToNextDockStartY = laneToNextDockStartY;
        this.laneToNextDockEndX = laneToNextDockEndX;
        this.laneToNextDockEndY = laneToNextDockEndY;
    }

    public boolean canEnterEnteringQ() {
        return enteringSemaphore.availablePermits() > 0;
    }

    public boolean canEnterExitingQ() {
        return exitingSemaphore.availablePermits() > 0;
    }

    public void enterEnteringQ(Vehicle vehicle) {
        try {
            enteringSemaphore.acquire();
            enteringQueue.add(vehicle);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void enterExitingQ(Vehicle vehicle) {
        try {
            exitingSemaphore.acquire();
            exitingQueue.add(vehicle);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Vehicle exitEnteringQ() {
        enteringSemaphore.release();
        return enteringQueue.poll();
    }

    public Vehicle exitExitingQ() {
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

    public double getFerryCoordinateX() {
        return ferryCoordinateX;
    }

    public double getFerryCoordinateY() {
        return ferryCoordinateY;
    }

    public int getCriticalSectionCoordinateX() {
        return criticalSectionCoordinateX;
    }

    public int getCriticalSectionCoordinateY() {
        return criticalSectionCoordinateY;
    }

    public int getCriticalSectionReturnCoordinateX() {
        return criticalSectionReturnCoordinateX;
    }

    public int getCriticalSectionReturnCoordinateY() {
        return criticalSectionReturnCoordinateY;
    }

    public int getFerryQueueSize() {
        return ferryQueueSize;
    }

    public int getFerryQueueCoordinateX() {
        return ferryQueueCoordinateX;
    }

    public int getFerryQueueCoordinateY() {
        return ferryQueueCoordinateY;
    }

    public int getLaneToNextDockStartX() {
        return laneToNextDockStartX;
    }

    public int getLaneToNextDockStartY() {
        return laneToNextDockStartY;
    }

    public int getLaneToNextDockEndX() {
        return laneToNextDockEndX;
    }

    public int getLaneToNextDockEndY() {
        return laneToNextDockEndY;
    }

    public Vehicle getCriticalSectionVehicle() {
        return criticalSectionVehicle;
    }

    public void setCriticalSectionVehicle(Vehicle vehicle) {
        this.criticalSectionVehicle = vehicle;
    }

    public Condition getCriticalSectionCondition() {
        return criticalSectionCondition;
    }

    public void signalVehicleToEnterCriticalSection() {
        criticalSectionLock.lock();
        try {
            criticalSectionCondition.signal();
        } finally {
            criticalSectionLock.unlock();
        }
    }

    public boolean isFerryAtDock() {
        return isFerryAtDock.get();
    }

    public void setFerryAtDock(boolean isFerryAtDock) {
        this.isFerryAtDock.set(isFerryAtDock);
    }

    public void reset() {
        enteringSemaphore.release(enteringSemaphore.availablePermits());
        exitingSemaphore.release(exitingSemaphore.availablePermits());
        enteringQueue.clear();
        exitingQueue.clear();
    }
}
