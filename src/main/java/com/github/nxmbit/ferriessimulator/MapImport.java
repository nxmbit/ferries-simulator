package com.github.nxmbit.ferriessimulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MapImport {
    private TileType[][] originalTileTypes;

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
}
