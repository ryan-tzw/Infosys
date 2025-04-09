package com.example.infosys.model;

public class GeoRect {
    double xMin, yMin, xMax, yMax;

    public GeoRect(double xMin, double yMin, double xMax, double yMax) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
    }

    boolean contains(Point p) {
        return (p.x >= xMin && p.x <= xMax && p.y >= yMin && p.y <= yMax);
    }

    boolean intersects(GeoRect other) {
        return !(other.xMin > xMax || other.xMax < xMin || other.yMin > yMax || other.yMax < yMin);
    }
}
