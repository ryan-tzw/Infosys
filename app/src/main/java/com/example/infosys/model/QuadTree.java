package com.example.infosys.model;

import java.util.ArrayList;
import java.util.List;

public class QuadTree {
    GeoRect boundary;
    int capacity;
    List<Point> points;
    boolean divided = false;
    QuadTree northeast, northwest, southeast, southwest;
    String quadId;

    public QuadTree(GeoRect boundary, int capacity) {
        this.boundary = boundary;
        this.capacity = capacity;
        this.points = new ArrayList<>();
    }

    public QuadTree(GeoRect boundary, int capacity, String quadId) {
        this(boundary, capacity);
        this.quadId = quadId;
    }

    public String getQuadId() {
        return quadId;
    }

    public boolean insert(Point point) {
        if (!boundary.contains(point)) return false;

        if (points.size() < capacity) {
            points.add(point);
            return true;
        } else {
            if (!divided) subdivide();
            return (northeast.insert(point) || northwest.insert(point) ||
                    southeast.insert(point) || southwest.insert(point));
        }
    }

    void subdivide() {
        double midX = (boundary.xMin + boundary.xMax) / 2;
        double midY = (boundary.yMin + boundary.yMax) / 2;

        northeast = new QuadTree(new GeoRect(midX, boundary.yMin, boundary.xMax, midY), capacity);
        northwest = new QuadTree(new GeoRect(boundary.xMin, boundary.yMin, midX, midY), capacity);
        southeast = new QuadTree(new GeoRect(midX, midY, boundary.xMax, boundary.yMax), capacity);
        southwest = new QuadTree(new GeoRect(boundary.xMin, midY, midX, boundary.yMax), capacity);

        divided = true;
    }

    public List<Point> query(GeoRect range, List<Point> found) {
        if (!boundary.intersects(range)) return found;

        for (Point p : points) {
            if (range.contains(p)) found.add(p);
        }

        if (divided) {
            northeast.query(range, found);
            northwest.query(range, found);
            southeast.query(range, found);
            southwest.query(range, found);
        }

        return found;
    }
}
