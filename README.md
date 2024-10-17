# Ferries Simulator

This project simulates the operation of multiple ferries transporting vehicles across a river. It is made in Java with JavaFX for the graphical interface and incorporates concurrent programming to handle the synchronized movement of ferries and vehicles.

## Table of Contents

- [Project Overview](#project-overview)
- [Build instructions](#build-instructions)
  - [Linux / MacOS](#linux--macos)
  - [Windows](#windows)


## Project Overview

The simulation involves multiple ferries, each with a specified capacity. Vehicles travel to and from the ferry dock along a single road, moving at varying speeds. 
If a faster vehicle approaches a slower one from behind, it adjusts its speed to match the slower vehicle and follows it.

At the dock, vehicles board ferries through a single shared entry/exit point, with priority given to vehicles disembarking. 
Ferries wait for either full capacity or a predefined maximum time before departing. 
If no vehicles are loaded within the allowed loading time, the ferry begins another waiting period, repeating this cycle until at least one vehicle boards. If the dock is occupied by another ferry, arriving ferries must wait in line until the dock becomes available.

The GUI, built using JavaFX, provides a real-time view of the simulation, and allows adjustments to settings such as the number of ferries, their capacity, vehicle speeds and more. The project makes use of Java's concurrency mechanisms to handle the synchronized processes of vehicle movement, ferry loading, and unloading (each ferry and each vehicle runs in its own thread.)

## Build instructions

> [!Important] <br>
> Make sure you have **Java Development Kit (JDK)** installed (version 21 or above)

First of all clone the repo and enter its directory:  
```
git clone https://github.com/nxmbit/ferries-simulator.git
cd ferries-simulator
```

### Linux / MacOS 

To run the application, without building jar file:

```
./mvnw javafx:run
```

To build a portable jar file with all dependencies included:
```
./mvnw clean install
```

The built jar file will be in ```target``` subdirectory. To run it type:
```
java -jar ferries-simulator-1.0.jar
```

### Windows

To run the application, without building jar file:

```
mvnw.cmd javafx:run
```

To build a portable jar file with all dependencies included:
```
mvnw.cmd clean install
```

The built jar file will be in ```target``` subdirectory. To run it type:
```
java -jar ferries-simulator-1.0.jar
```