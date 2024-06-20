package com.github.nxmbit.ferriessimulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.json.JSONObject;
import org.json.JSONTokener;

public class MapImport {
    private TileType[][] originalTileTypes;

    private int gridWidth;
    private int gridHeight;

    private int dockHeight;

    private int dock1EnteringCapacity;
    private int dock1ExitingCapacity;
    private double dock1FerryCoordinateX;
    private double dock1FerryCoordinateY;

    private int dock2EnteringCapacity;
    private int dock2ExitingCapacity;
    private double dock2FerryCoordinateX;
    private double dock2FerryCoordinateY;

    private int dock1FerryQueueSize;
    private int dock2FerryQueueSize;

    private int dock1CriticalSectionCoordinateX;
    private int dock1CriticalSectionCoordinateY;
    private int dock2CriticalSectionCoordinateX;
    private int dock2CriticalSectionCoordinateY;

    private int dock1CriticalSectionReturnCoordinateX;
    private int dock1CriticalSectionReturnCoordinateY;
    private int dock2CriticalSectionReturnCoordinateX;
    private int dock2CriticalSectionReturnCoordinateY;

    private int dock1FerryQueueCoordinateX;
    private int dock1FerryQueueCoordinateY;
    private int dock2FerryQueueCoordinateX;
    private int dock2FerryQueueCoordinateY;
    private int maxNumberOfFerries;

    private int dock1ToDock2LaneCoordinateX;
    private int dock1ToDock2LaneCoordinateY;
    private int dock1ToDock2LaneCoordinateXEnd;
    private int dock1ToDock2LaneCoordinateYEnd;
    private int dock1ToDock2GoDownToQueueCoordinateX;

    private int dock2ToDock1LaneCoordinateX;
    private int dock2ToDock1LaneCoordinateY;
    private int dock2ToDock1LaneCoordinateXEnd;
    private int dock2ToDock1LaneCoordinateYEnd;
    private int dock2ToDock1GoDownToQueueCoordinateX;


    public MapImport() {
        try (InputStream is = getClass().getResourceAsStream("/com/github/nxmbit/ferriessimulator/map_properties.json")) {
            JSONObject obj = new JSONObject(new JSONTokener(is));

            JSONObject grid = obj.getJSONObject("grid");
            this.gridWidth = grid.getInt("width");
            this.gridHeight = grid.getInt("height");
            this.dockHeight = grid.getInt("docks_height");
            this.maxNumberOfFerries = grid.getInt("max_number_of_ferries");
            this.dock1ToDock2LaneCoordinateX = grid.getJSONObject("dock1ToDock2LaneCoordinates").getInt("x");
            this.dock1ToDock2LaneCoordinateY = grid.getJSONObject("dock1ToDock2LaneCoordinates").getInt("y");
            this.dock1ToDock2LaneCoordinateXEnd = grid.getJSONObject("dock1ToDock2LaneCoordinates").getInt("x_end");
            this.dock1ToDock2LaneCoordinateYEnd = grid.getJSONObject("dock1ToDock2LaneCoordinates").getInt("y_end");
            this.dock1ToDock2GoDownToQueueCoordinateX = grid.getJSONObject("dock1ToDock2LaneCoordinates").getInt("x_go_down_to_queue");

            this.dock2ToDock1LaneCoordinateX = grid.getJSONObject("dock2ToDock1LaneCoordinates").getInt("x");
            this.dock2ToDock1LaneCoordinateY = grid.getJSONObject("dock2ToDock1LaneCoordinates").getInt("y");
            this.dock2ToDock1LaneCoordinateXEnd = grid.getJSONObject("dock2ToDock1LaneCoordinates").getInt("x_end");
            this.dock2ToDock1LaneCoordinateYEnd = grid.getJSONObject("dock2ToDock1LaneCoordinates").getInt("y_end");
            this.dock2ToDock1GoDownToQueueCoordinateX = grid.getJSONObject("dock2ToDock1LaneCoordinates").getInt("x_go_down_to_queue");

            JSONObject docks = obj.getJSONObject("docks");
            JSONObject dock1 = docks.getJSONObject("dock1");
            this.dock1EnteringCapacity = dock1.getInt("enteringVehicles");
            this.dock1ExitingCapacity = dock1.getInt("exitingVehicles");
            this.dock1FerryQueueSize = dock1.getInt("ferryQueueSize");
            this.dock1FerryCoordinateX = dock1.getJSONObject("ferryCoordinates").getDouble("x");
            this.dock1FerryCoordinateY = dock1.getJSONObject("ferryCoordinates").getDouble("y");
            this.dock1CriticalSectionCoordinateX = dock1.getJSONObject("criticalSectionCoordinates").getInt("x");
            this.dock1CriticalSectionCoordinateY = dock1.getJSONObject("criticalSectionCoordinates").getInt("y");
            this.dock1CriticalSectionReturnCoordinateX = dock1.getJSONObject("criticalSectionReturnCoordinates").getInt("x");
            this.dock1CriticalSectionReturnCoordinateY = dock1.getJSONObject("criticalSectionReturnCoordinates").getInt("y");
            this.dock1FerryQueueCoordinateX = dock1.getJSONObject("ferryQueueCoordinates").getInt("x");
            this.dock1FerryQueueCoordinateY = dock1.getJSONObject("ferryQueueCoordinates").getInt("y");

            JSONObject dock2 = docks.getJSONObject("dock2");
            this.dock2EnteringCapacity = dock2.getInt("enteringVehicles");
            this.dock2ExitingCapacity = dock2.getInt("exitingVehicles");
            this.dock2FerryQueueSize = dock2.getInt("ferryQueueSize");
            this.dock2FerryCoordinateX = dock2.getJSONObject("ferryCoordinates").getDouble("x");
            this.dock2FerryCoordinateY = dock2.getJSONObject("ferryCoordinates").getDouble("y");
            this.dock2CriticalSectionCoordinateX = dock2.getJSONObject("criticalSectionCoordinates").getInt("x");
            this.dock2CriticalSectionCoordinateY = dock2.getJSONObject("criticalSectionCoordinates").getInt("y");
            this.dock2CriticalSectionReturnCoordinateX = dock2.getJSONObject("criticalSectionReturnCoordinates").getInt("x");
            this.dock2CriticalSectionReturnCoordinateY = dock2.getJSONObject("criticalSectionReturnCoordinates").getInt("y");
            this.dock2FerryQueueCoordinateX = dock2.getJSONObject("ferryQueueCoordinates").getInt("x");
            this.dock2FerryQueueCoordinateY = dock2.getJSONObject("ferryQueueCoordinates").getInt("y");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TileType convertToTileType(String tileSymbol) {
        switch (tileSymbol) {
            case "W":
                return TileType.WATER;
            case "R":
                return TileType.ROAD;
            case "REL":
                return TileType.ROAD_EDGE_LEFT;
            case "RER":
                return TileType.ROAD_EDGE_RIGHT;
            case "RD":
                return TileType.ROAD_DIVIDER;
            case "D":
                return TileType.DOCK;
            case "DDH":
                return TileType.DOCK_DIVIDER_HORIZONTAL;
            case "DDV":
                return TileType.DOCK_DIVIDER_VERTICAL;
            case "DDCR":
                return TileType.DOCK_DIVIDER_CROSS_RIGHT;
            case "DDCL":
                return TileType.DOCK_DIVIDER_CROSS_LEFT;
            case "DECR":
                return TileType.DOCK_EDGE_CROSS_RIGHT;
            case "DECL":
                return TileType.DOCK_EDGE_CROSS_LEFT;
            case "DEBCR":
                return TileType.DOCK_EDGE_BOTTOM_CONN_RIGHT;
            case "DEBCL":
                return TileType.DOCK_EDGE_BOTTOM_CONN_LEFT;
            case "DEL":
                return TileType.DOCK_EDGE_LEFT;
            case "DER":
                return TileType.DOCK_EDGE_RIGHT;
            case "DET":
                return TileType.DOCK_EDGE_TOP;
            case "DEB":
                return TileType.DOCK_EDGE_BOTTOM;
            case "DTL":
                return TileType.DOCK_TURN_LEFT;
            case "DTR":
                return TileType.DOCK_TURN_RIGHT;
            case "DSD":
                return TileType.DOCK_STRAIGHT_DOWN;
            case "DC":
                return TileType.DOCK_CRITICAL_SECTION;
            case "DQ":
                return TileType.DOCK_QUEUE;
            case "G":
                return TileType.GRASS;
            case "B":
                return TileType.BEACH;
            case "RS1":
                return TileType.ROAD_SPAWN_DOCK_1;
            case "RS2":
                return TileType.ROAD_SPAWN_DOCK_2;
            case "RQ1":
                return TileType.ROAD_QUIT_DOCK_1;
            case "RQ2":
                return TileType.ROAD_QUIT_DOCK_2;
            default:
                throw new IllegalArgumentException("Invalid tile symbol: " + tileSymbol);
        }
    }

    public Tile[][] generate(int gridWidth, int gridHeight, double tileSize) {
        Tile[][] grid = new Tile[gridWidth][gridHeight];
        originalTileTypes = new TileType[gridWidth][gridHeight];

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/com/github/nxmbit/ferriessimulator/map.csv")))) {
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                String[] tileSymbols = line.split(",");
                for (int j = 0; j < tileSymbols.length; j++) {
                    TileType type = convertToTileType(tileSymbols[j]);
                    Tile tile = new Tile(j * tileSize, i * tileSize, tileSize, tileSize, type, j, i);
                    grid[j][i] = tile;
                    originalTileTypes[j][i] = type; // Store the original type
                }
                i++;
            }
        } catch (IOException e) {
            System.err.println("Error reading the map file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing the map file: " + e.getMessage());
        }

        return grid;
    }

    public TileType[][] getOriginalTileTypes() {
        return originalTileTypes;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public int getDockHeight() {
        return dockHeight;
    }

    public int getDock1EnteringCapacity() {
        return dock1EnteringCapacity;
    }

    public int getDock1ExitingCapacity() {
        return dock1ExitingCapacity;
    }

    public double getDock1FerryCoordinateX() {
        return dock1FerryCoordinateX;
    }

    public double getDock1FerryCoordinateY() {
        return dock1FerryCoordinateY;
    }

    public int getDock2EnteringCapacity() {
        return dock2EnteringCapacity;
    }

    public int getDock2ExitingCapacity() {
        return dock2ExitingCapacity;
    }

    public double getDock2FerryCoordinateX() {
        return dock2FerryCoordinateX;
    }

    public double getDock2FerryCoordinateY() {
        return dock2FerryCoordinateY;
    }

    public int getDock1CriticalSectionCoordinateX() {
        return dock1CriticalSectionCoordinateX;
    }

    public int getDock1CriticalSectionCoordinateY() {
        return dock1CriticalSectionCoordinateY;
    }

    public int getDock2CriticalSectionCoordinateX() {
        return dock2CriticalSectionCoordinateX;
    }

    public int getDock2CriticalSectionCoordinateY() {
        return dock2CriticalSectionCoordinateY;
    }

    public int getDock1CriticalSectionReturnCoordinateX() {
        return dock1CriticalSectionReturnCoordinateX;
    }

    public int getDock1CriticalSectionReturnCoordinateY() {
        return dock1CriticalSectionReturnCoordinateY;
    }

    public int getDock2CriticalSectionReturnCoordinateX() {
        return dock2CriticalSectionReturnCoordinateX;
    }

    public int getDock2CriticalSectionReturnCoordinateY() {
        return dock2CriticalSectionReturnCoordinateY;
    }

    public int getDock1FerryQueueSize() {
        return dock1FerryQueueSize;
    }

    public int getDock2FerryQueueSize() {
        return dock2FerryQueueSize;
    }

    public int getDock1FerryQueueCoordinateX() {
        return dock1FerryQueueCoordinateX;
    }

    public int getDock1FerryQueueCoordinateY() {
        return dock1FerryQueueCoordinateY;
    }

    public int getDock2FerryQueueCoordinateX() {
        return dock2FerryQueueCoordinateX;
    }

    public int getDock2FerryQueueCoordinateY() {
        return dock2FerryQueueCoordinateY;
    }

    public int getMaxNumberOfFerries() {
        return maxNumberOfFerries;
    }

    public int getDock1ToDock2LaneCoordinateX() {
        return dock1ToDock2LaneCoordinateX;
    }

    public int getDock1ToDock2LaneCoordinateY() {
        return dock1ToDock2LaneCoordinateY;
    }

    public int getDock2ToDock1LaneCoordinateX() {
        return dock2ToDock1LaneCoordinateX;
    }

    public int getDock2ToDock1LaneCoordinateY() {
        return dock2ToDock1LaneCoordinateY;
    }

    public int getDock1ToDock2LaneCoordinateXEnd() {
        return dock1ToDock2LaneCoordinateXEnd;
    }

    public int getDock1ToDock2LaneCoordinateYEnd() {
        return dock1ToDock2LaneCoordinateYEnd;
    }

    public int getDock2ToDock1LaneCoordinateXEnd() {
        return dock2ToDock1LaneCoordinateXEnd;
    }

    public int getDock2ToDock1LaneCoordinateYEnd() {
        return dock2ToDock1LaneCoordinateYEnd;
    }

    public int getDock1ToDock2GoDownToQueueCoordinateX() {
        return dock1ToDock2GoDownToQueueCoordinateX;
    }

    public int getDock2ToDock1GoDownToQueueCoordinateX() {
        return dock2ToDock1GoDownToQueueCoordinateX;
    }
}
