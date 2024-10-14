package com.github.nxmbit.ferriessimulator;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Tile extends Rectangle {
    private TileType type;
    private final Lock lock;
    private int gridX;
    private int gridY;
    private boolean strokeVisibility;

    public Tile(double paneX, double paneY, double width, double height, TileType type, int gridX, int gridY) {
        super(paneX, paneY, width, height);
        this.gridX = gridX;
        this.gridY = gridY;
        this.type = type;
        this.lock = new ReentrantLock();
        setFillBasedOnType();
    }

    public Lock getLock() {
        return lock;
    }

    public void setStrokeVisibility(boolean visibility) {
        strokeVisibility = visibility;
        setFillBasedOnType();
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public TileType getType() {
        return type;
    }

    public void setType(TileType type) {
        this.type = type;
        setFillBasedOnType();
    }

    public boolean isRoadOfAnyType() {
        return type == TileType.ROAD || type == TileType.ROAD_DIVIDER || type == TileType.ROAD_SPAWN_DOCK_1 || type == TileType.ROAD_SPAWN_DOCK_2 || type == TileType.ROAD_QUIT_DOCK_1 || type == TileType.ROAD_QUIT_DOCK_2;
    }

    public static boolean isRoadOfAnyType(TileType tile) {
        return tile == TileType.ROAD || tile == TileType.ROAD_DIVIDER || tile == TileType.ROAD_SPAWN_DOCK_1 || tile == TileType.ROAD_SPAWN_DOCK_2 || tile == TileType.ROAD_QUIT_DOCK_1 || tile == TileType.ROAD_QUIT_DOCK_2;
    }

    private void setFillBasedOnType() {
        if (strokeVisibility) {
            setStroke(Color.BLACK);
            setStrokeWidth(1);
        } else {
            setStroke(null);
        }

        setSmooth(false);
        switch (type) {
            case WATER:
                setFill(Color.BLUE);
                break;
            case ROAD:
            case ROAD_SPAWN_DOCK_1:
            case ROAD_SPAWN_DOCK_2:
            case ROAD_QUIT_DOCK_1:
            case ROAD_QUIT_DOCK_2:
                setFill(Color.GRAY);
                break;
            case ROAD_EDGE_LEFT:
                setFill(ImageLoader.getRoadEdgeLeftImagePattern());
                break;
            case ROAD_EDGE_RIGHT:
                setFill(ImageLoader.getRoadEdgeRightImagePattern());
                break;
            case ROAD_DIVIDER:
                setFill(ImageLoader.getRoadDividerImagePattern());
                break;
            case DOCK:
            case DOCK_STRAIGHT_DOWN:
            case DOCK_TURN_LEFT:
            case DOCK_TURN_RIGHT:
                setFill(Color.DARKGRAY);
                break;
            case DOCK_DIVIDER_HORIZONTAL:
                setFill(ImageLoader.getDockDividerHorizontalImagePattern());
                break;
            case DOCK_DIVIDER_VERTICAL:
                setFill(ImageLoader.getDockDividerVerticalImagePattern());
                break;
            case DOCK_DIVIDER_CROSS_RIGHT:
                setFill(ImageLoader.getDockDividerCrossRightImagePattern());
                break;
            case DOCK_DIVIDER_CROSS_LEFT:
                setFill(ImageLoader.getDockDividerCrossLeftImagePattern());
                break;
            case DOCK_EDGE_LEFT:
                setFill(ImageLoader.getDockEdgeLeftImagePattern());
                break;
            case DOCK_EDGE_RIGHT:
                setFill(ImageLoader.getDockEdgeRightImagePattern());
                break;
            case DOCK_EDGE_TOP:
                setFill(ImageLoader.getDockEdgeTopImagePattern());
                break;
            case DOCK_EDGE_BOTTOM:
                setFill(ImageLoader.getDockEdgeBottomImagePattern());
                break;
            case DOCK_EDGE_CROSS_RIGHT:
                setFill(ImageLoader.getDockEdgeCrossRightImagePattern());
                break;
            case DOCK_EDGE_CROSS_LEFT:
                setFill(ImageLoader.getDockEdgeCrossLeftImagePattern());
                break;
            case DOCK_EDGE_BOTTOM_CONN_RIGHT:
                setFill(ImageLoader.getDockEdgeBottomConnRightImagePattern());
                break;
            case DOCK_EDGE_BOTTOM_CONN_LEFT:
                setFill(ImageLoader.getDockEdgeBottomConnLeftImagePattern());
                break;
            case DOCK_CRITICAL_SECTION:
                setFill(Color.BLACK);
                break;
            case DOCK_QUEUE:
                setFill(Color.DIMGRAY);
                break;
            case GRASS:
                setFill(Color.GREEN);
                break;
            case BEACH:
                setFill(Color.SANDYBROWN);
                break;
            case VEHICLE:
                setFill(Color.CYAN);
                break;
            case FERRY:
                setFill(Color.RED);
                break;
            default:
                setFill(Color.WHITE);
                break;
        }
    }
}
