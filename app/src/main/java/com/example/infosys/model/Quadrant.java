package com.example.infosys.model;

public class Quadrant {
    private final int xIndex;
    private final int yIndex;
    private final double tileSize;

    public Quadrant(double lat, double lng, double tileSize) {
        this.tileSize = tileSize;
        this.xIndex = (int) Math.floor((lng + 180.0) / tileSize);
        this.yIndex = (int) Math.floor((lat + 90.0) / tileSize);
    }

    public String getId() {
        return xIndex + "_" + yIndex;
    }

    public GeoRect getBounds() {
        double xMin = xIndex * tileSize - 180.0;
        double xMax = xMin + tileSize;
        double yMin = yIndex * tileSize - 90.0;
        double yMax = yMin + tileSize;
        return new GeoRect(xMin, yMin, xMax, yMax);
    }
}
