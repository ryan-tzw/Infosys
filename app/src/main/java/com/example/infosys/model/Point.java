package com.example.infosys.model;

public class Point {
    double x, y; // longitude, latitude
    String userId;

    public Point(double x, double y, String userId) {
        this.x = x;
        this.y = y;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
