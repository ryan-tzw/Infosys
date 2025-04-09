package com.example.infosys.model;

import java.util.ArrayList;
import java.util.List;

class QuadTree {
    Rect boundary;
    int capacity;
    List<Point> points;
    boolean divided = false;
    QuadTree northeast, northwest, southeast, southwest;

    QuadTree(Rect boundary, int capacity) {
        this.boundary = boundary;
        this.capacity = capacity;
        this.points = new ArrayList<>();
    }

    boolean insert(Point point) {
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

        northeast = new QuadTree(new Rect(midX, boundary.yMin, boundary.xMax, midY), capacity);
        northwest = new QuadTree(new Rect(boundary.xMin, boundary.yMin, midX, midY), capacity);
        southeast = new QuadTree(new Rect(midX, midY, boundary.xMax, boundary.yMax), capacity);
        southwest = new QuadTree(new Rect(boundary.xMin, midY, midX, boundary.yMax), capacity);

        divided = true;
    }

    List<Point> query(Rect range, List<Point> found) {
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
