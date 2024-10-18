# Ferries Simulator

This project simulates the operation of multiple ferries transporting vehicles across a river. It is made in Java with JavaFX for the graphical interface and incorporates concurrent programming to handle the synchronized movement of ferries and vehicles.

![ferries_simulator](https://github.com/user-attachments/assets/3edcc769-969c-4299-ac5d-10f842258bd9)

## Table of Contents

- [Project Overview](#project-overview)
- [Build instructions](#build-instructions)
  - [Linux / MacOS](#linux--macos)
  - [Windows](#windows)
- [Usage](#usage)
  - [Simulation view](#simulation-view)
  - [Changing settings of the simulation](#changing-settings-of-the-simulation)


## Project Overview

The simulation involves multiple ferries that transport vehicles across the river. Vehicles travel to and from the ferry dock along a single road, moving at varying speeds. 
If a faster vehicle approaches a slower one from behind, it adjusts its speed to match the slower vehicle and follows it.

When a vehicle reaches the dock, it will either board a ferry or wait in the queue.
At the dock, vehicles board ferries through a single shared entry/exit point, with priority given to vehicles disembarking. 
Ferries wait for either full capacity or a predefined maximum time before departing. 
If no vehicles are loaded within the allowed loading time, the ferry begins another waiting period, repeating this cycle until at least one vehicle boards. 
If the dock is occupied by another ferry, arriving ferries must wait in line until the dock becomes available.

Once vehicles disembark on the other side of the river, they drive off the dock and continue to the end of the road where they despawn (the vehicle's thread ends).

The GUI, built using JavaFX, provides a real-time view of the simulation, 
and allows adjustments to settings such as the number of ferries, their capacity,
vehicle speeds and more. The project makes use of Java's concurrency mechanisms to handle the synchronized processes of vehicle movement,
ferry loading, and unloading (each ferry and each vehicle runs in its own thread).

## Build instructions

> [!IMPORTANT]  
> Make sure you have **Java Development Kit (JDK)** installed (version 21 or above).

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

## Usage

After opening the application, you will see the following window:  

![Main_window](https://github.com/user-attachments/assets/be14e820-3c4d-4d0f-99de-e08c5b56a6a0)

### Simulation view

The simulation view is the main part of the window. It shows the visual representation of the simulation.

Below is a description of the elements on the simulation view (for simplicity, only one side of the river is shown):

<table>
  <tr>
    <td rowspan="10">
      <img src="https://github.com/user-attachments/assets/28c9ae9d-7d90-493c-892b-02bf0ae66368" alt="Simulation view">
    </td>
    <td>
      <strong>1. Vehicle spawn point</strong>
    </td>
  </tr>
  <tr>
    <td><strong>2. Vehicle despawn point</strong></td>
  </tr>
  <tr>
    <td><strong>3. Road lane leading to the dock</strong></td>
  </tr>
  <tr>
    <td><strong>4. Road lane leading from the dock</strong></td>
  </tr>
  <tr>
    <td>
      <strong>5. Ferry queue</strong><br>
      (This is where ferries wait for their turn to board the ferry)
    </td>
  </tr>
  <tr>
    <td><strong>6. Vehicle</strong></td>
  </tr>
  <tr>
    <td><strong>7. Queue at the dock for boarding the ferry</strong></td>
  </tr>
  <tr>
    <td><strong>8. Queue at the dock for disembarking from the ferry</strong></td>
  </tr>
    <tr>
    <td><strong>9. Shared ferry entry/exit point</strong></td>
  </tr>
    <tr>
    <td>
  <strong>10. Ferry</strong><br>
  Additional info:
  <ul>
    <li>The <strong>maximum capacity of the ferry</strong> is shown in red text in the top-left corner.</li>
    <li>The <strong>current number of loaded vehicles</strong> is displayed in white text in the center of the ferry.</li>
    <li>The <strong>remaining loading time</strong> (in seconds) is shown in white text at the bottom of the ferry.</li>
  </ul>
</td>
  </tr>
</table>

### Changing settings of the simulation

> [!NOTE]  
> The default settings can be changed in the ```settings.json``` file located in the resources directory.

> [!IMPORTANT]  
> Some default settings (e.g., queue sizes at docks) are loaded from the ```map_properties.json``` file.
> The values in this file define the properties and limitations of the map.
> Changing these values is not recommended and may lead to unexpected behavior. 

On the left side of the window there is a control panel with the following expandable sections:

![Control_panel](https://github.com/user-attachments/assets/32d0ec9d-d527-474a-ae19-f5f25b77f944)

- **Simulation Controls**: Here you can start or stop and reset the simulation to its initial state.  
  ![simulation_controls](https://github.com/user-attachments/assets/18052a68-38e9-496f-b1c0-2e2bcc791e0e)

- **Vehicles**: Here you can adjust the max number of vehicles, the min and max speed of vehicles, 
and the spawn rate of vehicles. You can additionally set the probability on which road leading to 
the dock the vehicle will spawn and manually spawn a vehicle. Note that this section is only active
when the simulation is running.  
  ![Vehicles](https://github.com/user-attachments/assets/a18ab73c-54be-496c-a7b9-577f6d4c1f7d)

- **Left dock/Right dock**: As previously stated, vehicles can spawn on roads leading to the left or right dock.
Each dock has limited capacity and can only accommodate a certain number of vehicles. Here you can adjust the
size of queues on the docks. At the dock, vehicles board ferries through a single shared entry/exit point, with priority given to vehicles disembarking. 
There are two queues at the dock: one for vehicles boarding the ferry and another for vehicles leaving the ferry.
Note that this section is only active when the simulation is not running.  
  ![l_r_dock](https://github.com/user-attachments/assets/9c18e617-b776-426d-a22c-f55b0a5d395f)
  
- **Ferries**: Here you can adjust the number of ferries spawned on the queue to the left and right docks,
min/max capacity of ferries, speed of ferries and max/min loading time for spawned ferries.
Note that this section is only active when the simulation is not running.  
  ![ferries](https://github.com/user-attachments/assets/a7887a3c-ecb3-41a0-9530-4223fa6a81db)

- **View Options**: Contains an option to show/hide the grid on the simulation window. Only for testing.  
  