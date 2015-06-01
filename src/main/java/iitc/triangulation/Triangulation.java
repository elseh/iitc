package iitc.triangulation;

import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.Triple;

import java.util.*;

/**
 * Created by Sigrlinn on 31.05.2015.
 */
public class Triangulation {
    private Map<Point, Set<Point>> outComingLinks = new HashMap<>();
    private Field baseField;

    public Triangulation(List<Point> allPoints, Triple<Point> baseTriangle) {
        baseField = new Field(baseTriangle, allPoints);
        outComingLinks.computeIfAbsent(baseTriangle.v1, k -> new HashSet<>()).add(baseTriangle.v2);
        outComingLinks.computeIfAbsent(baseTriangle.v1, k -> new HashSet<>()).add(baseTriangle.v3);
        outComingLinks.computeIfAbsent(baseTriangle.v2, k -> new HashSet<>()).add(baseTriangle.v3);
        //baseTriangle.split().stream().forEach(p -> outComingLinks.computeIfAbsent(p.v1, k -> new HashSet<>()).add(p.v2));
    }

    public boolean triangulateField(Field field) {
        if (field.getInners().isEmpty()) {
            return true;
        }
        Triple<Point> bases = field.getBases();

        if (bases.stream().anyMatch(p -> !checkPoint(p, 7))) {
            return false;
        }

        List<Point> inners = new ArrayList<>(field.getInners());
        inners.sort(Comparator.comparing(p -> GeoUtils.getDistance(p.getLatlng(), GeoUtils.getCenter(bases.simplify(Point::getLatlng)))));
        for (Point point : inners) {
            field.insertSmallerFields(point);
            bases.stream().forEach(p -> outComingLinks.get(p).add(point));
            if (field.getSmallerFields().stream().allMatch(this::triangulateField)) {
                return true;
            }

            field.resetSmallerFields();
            bases.stream().forEach(p -> outComingLinks.get(p).removeAll(inners));
            inners.stream().forEach(p -> { if (outComingLinks.containsKey(p)) outComingLinks.get(p).clear();});
        }
        return false;
    }

    public boolean run() {
        return triangulateField(baseField);
    }

    public boolean checkPoint(Point p, int amount) {
        return outComingLinks.computeIfAbsent(p, v-> new HashSet<>()).size() <= amount;
    }

    public Field getBaseField() {
        return baseField;
    }
}
