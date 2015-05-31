package iitc.triangulation;

import java.util.*;

/**
 * Created by Sigrlinn on 31.05.2015.
 */
public class Triangulation {
    private Map<Point, Set<Point>> outComingLinks = new HashMap<>();
    private Map<Point, Integer> outCounters = new HashMap<>();
    private Field baseField;

    public Triangulation(List<Point> allPoints, GeoUtils.Triple<Point> baseTriangle) {
        baseField = new Field(baseTriangle, allPoints);
        add(baseTriangle.v1, baseTriangle.v2);
        add(baseTriangle.v2, baseTriangle.v3);
        add(baseTriangle.v3, baseTriangle.v1);
        /*outCounters.put(baseTriangle.v1, 2);
        outCounters.put(baseTriangle.v2, 2);
        outCounters.put(baseTriangle.v3, 2);*/
    }

    public boolean triangulateField(Field field) {
        if (field.getInners().isEmpty()) {
            return true;
        }
        GeoUtils.Triple<Point> bases = field.getBases();
        if (!checkPoint(bases.v1, 7) || !checkPoint(bases.v2, 7) || !checkPoint(bases.v3, 7)) {
            return false;
        }
        /*add(bases.v1, 1);
        add(bases.v2, 1);
        add(bases.v3, 1);*/
        for (Point point : field.getInners()) {
            field.insertSmallerFields(point);

            outComingLinks.get(bases.v1).add(point);
            outComingLinks.get(bases.v2).add(point);
            outComingLinks.get(bases.v3).add(point);

            GeoUtils.Triple<Field> smallerFields = field.getSmallerFields();
            if (triangulateField(smallerFields.v1) && triangulateField(smallerFields.v2) && triangulateField(smallerFields.v3)) {
                return true;
            }
            outComingLinks.get(bases.v1).remove(point);
            outComingLinks.get(bases.v2).remove(point);
            outComingLinks.get(bases.v3).remove(point);
        }
        /*add(bases.v1, -1);
        add(bases.v2, -1);
        add(bases.v3, -1);*/
        return false;
    }

    public boolean run() {
        return triangulateField(baseField);
    }

    public boolean checkPoint(Point p, int amount) {
        if (!outComingLinks.containsKey(p)) {
            outComingLinks.put(p, new HashSet<>());
        }
        return outComingLinks.get(p).size() <= amount;
        /*if (!outCounters.containsKey(p)) {
            outCounters.put(p, 0);
        }
        return (outCounters.get(p) <= amount);*/
    }

    public void add(Point p, int amount) {
        outCounters.put(p, outCounters.getOrDefault(p, 0) + amount);
    }

    public void add(Point p1, Point p2) {
        if (!outComingLinks.containsKey(p1)) {
            outComingLinks.put(p1, new HashSet<>());
        }
        outComingLinks.get(p1).add(p2);
    }

    public Field getBaseField() {
        return baseField;
    }
}
