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
    private int dock1CriticalSectionCoordinateX;
    private int dock1CriticalSectionCoordinateY;
    private int dock2CriticalSectionCoordinateX;
    private int dock2CriticalSectionCoordinateY;
    private int dock1CriticalSectionReturnCoordinateX;
    private int dock1CriticalSectionReturnCoordinateY;
    private int dock2CriticalSectionReturnCoordinateX;
    private int dock2CriticalSectionReturnCoordinateY;

    public MapImport() {
        try (InputStream is = getClass().getResourceAsStream("/com/github/nxmbit/ferriessimulator/map_properties.json")) {
            JSONObject obj = new JSONObject(new JSONTokener(is));

            JSONObject grid = obj.getJSONObject("grid");
            this.gridWidth = grid.getInt("width");
            this.gridHeight = grid.getInt("height");
            this.dockHeight = grid.getInt("docks_height");

            JSONObject docks = obj.getJSONObject("docks");
            JSONObject dock1 = docks.getJSONObject("dock1");
            this.dock1EnteringCapacity = dock1.getInt("enteringVehicles");
            this.dock1ExitingCapacity = dock1.getInt("exitingVehicles");
            this.dock1FerryCoordinateX = dock1.getJSONObject("ferryCoordinates").getDouble("x");
            this.dock1FerryCoordinateY = dock1.getJSONObject("ferryCoordinates").getDouble("y");
            this.dock1CriticalSectionCoordinateX = dock1.getJSONObject("criticalSectionCoordinates").getInt("x");
            this.dock1CriticalSectionCoordinateY = dock1.getJSONObject("criticalSectionCoordinates").getInt("y");
            this.dock1CriticalSectionReturnCoordinateX = dock1.getJSONObject("criticalSectionReturnCoordinates").getInt("x");
            this.dock1CriticalSectionReturnCoordinateY = dock1.getJSONObject("criticalSectionReturnCoordinates").getInt("y");

            JSONObject dock2 = docks.getJSONObject("dock2");
            this.dock2EnteringCapacity = dock2.getInt("enteringVehicles");
            this.dock2ExitingCapacity = dock2.getInt("exitingVehicles");
            this.dock2FerryCoordinateX = dock2.getJSONObject("ferryCoordinates").getDouble("x");
            this.dock2FerryCoordinateY = dock2.getJSONObject("ferryCoordinates").getDouble("y");
            this.dock2CriticalSectionCoordinateX = dock2.getJSONObject("criticalSectionCoordinates").getInt("x");
            this.dock2CriticalSectionCoordinateY = dock2.getJSONObject("criticalSectionCoordinates").getInt("y");
            this.dock2CriticalSectionReturnCoordinateX = dock2.getJSONObject("criticalSectionReturnCoordinates").getInt("x");
            this.dock2CriticalSectionReturnCoordinateY = dock2.getJSONObject("criticalSectionReturnCoordinates").getInt("y");
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
}
