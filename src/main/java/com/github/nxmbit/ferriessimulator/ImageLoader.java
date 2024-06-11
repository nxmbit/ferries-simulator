package com.github.nxmbit.ferriessimulator;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

public class ImageLoader {
    private static ImagePattern roadEdgeRightImagePattern;
    private static ImagePattern roadEdgeLeftImagePattern;
    private static ImagePattern roadDividerImagePattern;
    private static ImagePattern dockDividerCrossLeftImagePattern;
    private static ImagePattern dockDividerCrossRightImagePattern;
    private static ImagePattern dockDividerHorizontalImagePattern;
    private static ImagePattern dockDividerVerticalImagePattern;
    private static ImagePattern dockEdgeBottomImagePattern;
    private static ImagePattern dockEdgeCrossLeftImagePattern;
    private static ImagePattern dockEdgeCrossRightImagePattern;
    private static ImagePattern dockEdgeTopImagePattern;
    private static ImagePattern dockEdgeLeftImagePattern;
    private static ImagePattern dockEdgeRightImagePattern;

    static {
        roadEdgeRightImagePattern = new ImagePattern(new Image(ImageLoader.class.getResourceAsStream("/com/github/nxmbit/ferriessimulator/images/ROAD_EDGE_RIGHT.png")));
        roadEdgeLeftImagePattern = new ImagePattern(new Image(ImageLoader.class.getResourceAsStream("/com/github/nxmbit/ferriessimulator/images/ROAD_EDGE_LEFT.png")));
        roadDividerImagePattern = new ImagePattern(new Image(ImageLoader.class.getResourceAsStream("/com/github/nxmbit/ferriessimulator/images/ROAD_DIVIDER.png")));
        dockDividerCrossLeftImagePattern = new ImagePattern(new Image(ImageLoader.class.getResourceAsStream("/com/github/nxmbit/ferriessimulator/images/DOCK_DIVIDER_CROSS_LEFT.png")));
        dockDividerCrossRightImagePattern = new ImagePattern(new Image(ImageLoader.class.getResourceAsStream("/com/github/nxmbit/ferriessimulator/images/DOCK_DIVIDER_CROSS_RIGHT.png")));
        dockDividerHorizontalImagePattern = new ImagePattern(new Image(ImageLoader.class.getResourceAsStream("/com/github/nxmbit/ferriessimulator/images/DOCK_DIVIDER_HORIZONTAL.png")));
        dockDividerVerticalImagePattern = new ImagePattern(new Image(ImageLoader.class.getResourceAsStream("/com/github/nxmbit/ferriessimulator/images/DOCK_DIVIDER_VERTICAL.png")));
        dockEdgeBottomImagePattern = new ImagePattern(new Image(ImageLoader.class.getResourceAsStream("/com/github/nxmbit/ferriessimulator/images/DOCK_EDGE_BOTTOM.png")));
        dockEdgeCrossLeftImagePattern = new ImagePattern(new Image(ImageLoader.class.getResourceAsStream("/com/github/nxmbit/ferriessimulator/images/DOCK_EDGE_CROSS_LEFT.png")));
        dockEdgeCrossRightImagePattern = new ImagePattern(new Image(ImageLoader.class.getResourceAsStream("/com/github/nxmbit/ferriessimulator/images/DOCK_EDGE_CROSS_RIGHT.png")));
        dockEdgeTopImagePattern = new ImagePattern(new Image(ImageLoader.class.getResourceAsStream("/com/github/nxmbit/ferriessimulator/images/DOCK_EDGE_TOP.png")));
        dockEdgeLeftImagePattern = new ImagePattern(new Image(ImageLoader.class.getResourceAsStream("/com/github/nxmbit/ferriessimulator/images/DOCK_EDGE_LEFT.png")));
        dockEdgeRightImagePattern = new ImagePattern(new Image(ImageLoader.class.getResourceAsStream("/com/github/nxmbit/ferriessimulator/images/DOCK_EDGE_RIGHT.png")));
    }

    public static ImagePattern getRoadEdgeRightImagePattern() {
        return roadEdgeRightImagePattern;
    }

    public static ImagePattern getRoadEdgeLeftImagePattern() {
        return roadEdgeLeftImagePattern;
    }

    public static ImagePattern getRoadDividerImagePattern() {
        return roadDividerImagePattern;
    }

    public static ImagePattern getDockDividerCrossLeftImagePattern() {
        return dockDividerCrossLeftImagePattern;
    }

    public static ImagePattern getDockDividerCrossRightImagePattern() {
        return dockDividerCrossRightImagePattern;
    }

    public static ImagePattern getDockDividerHorizontalImagePattern() {
        return dockDividerHorizontalImagePattern;
    }

    public static ImagePattern getDockDividerVerticalImagePattern() {
        return dockDividerVerticalImagePattern;
    }

    public static ImagePattern getDockEdgeBottomImagePattern() {
        return dockEdgeBottomImagePattern;
    }

    public static ImagePattern getDockEdgeCrossLeftImagePattern() {
        return dockEdgeCrossLeftImagePattern;
    }

    public static ImagePattern getDockEdgeCrossRightImagePattern() {
        return dockEdgeCrossRightImagePattern;
    }

    public static ImagePattern getDockEdgeTopImagePattern() {
        return dockEdgeTopImagePattern;
    }

    public static ImagePattern getDockEdgeLeftImagePattern() {
        return dockEdgeLeftImagePattern;
    }

    public static ImagePattern getDockEdgeRightImagePattern() {
        return dockEdgeRightImagePattern;
    }
}