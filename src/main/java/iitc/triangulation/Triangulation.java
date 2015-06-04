package iitc.triangulation;

import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.LatLngs;
import iitc.triangulation.shapes.Triple;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sigrlinn on 31.05.2015.
 */
public class Triangulation {
    private Map<Point, Set<Point>> outComingLinks = new HashMap<>();
    private Set<Point> unprocessedPoints = new HashSet<>();
    private Set<Field> baseFields;

    public Triangulation(List<Point> allPoints, Set<Triple<Point>> baseTriangles, Map<Point, Set<Point>> outLinks) {
        baseFields = baseTriangles.stream().map(t -> new Field(t, allPoints)).collect(Collectors.toSet());
        outComingLinks.putAll(outLinks);
        unprocessedPoints = baseFields.stream().flatMap(f -> f.getInners().stream()).collect(Collectors.toSet());
    }


        /*baseField = new Field(baseTriangle, allPoints);
        outComingLinks.computeIfAbsent(baseTriangle.v1, k -> new HashSet<>()).add(baseTriangle.v2);
        outComingLinks.computeIfAbsent(baseTriangle.v1, k -> new HashSet<>()).add(baseTriangle.v3);
        outComingLinks.computeIfAbsent(baseTriangle.v2, k -> new HashSet<>()).add(baseTriangle.v3);
        //baseTriangle.split().stream().forEach(p -> outComingLinks.computeIfAbsent(p.v1, k -> new HashSet<>()).add(p.v2));
    }*/



    public boolean triangulateField(Field field) {
        if (field.getInners().isEmpty()) {
            return true;
        }
        Triple<Point> bases = field.getBases();

        if (bases.stream().anyMatch(p -> !checkPoint(p, 7))) {
            return false;
        }

        List<Point> inners = new ArrayList<>(field.getInners());
        LatLngs center = GeoUtils.getCenter(bases.simplify(Point::getLatlng));
        inners.sort(Comparator.comparing(p -> GeoUtils.getDistance(p.getLatlng(), center)));
        //inners.sort(Comparator.comparing(this::minDistance));
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

    public boolean triangulateFields(Set<Point> points) {
        if (unprocessedPoints.isEmpty()) return true;
        Set<Point> unprocessed = new HashSet<>(points);
        for (Point p : points) {
            unprocessed.remove(p);
            Field field = searchField(p, baseFields);
            if (field.getBases().stream().allMatch(b -> ! checkPoint(b, 7))) {
                return false;
            }
            field.insertSmallerFields(p);
            field.getBases().stream().forEach(b -> outComingLinks.get(b).add(p));
            if (triangulateFields(unprocessed)) {
                return true;
            } else {
                unprocessed.add(p);
                field.resetSmallerFields();
                field.getBases().stream().forEach(b -> outComingLinks.get(b).removeAll(field.getInners()));
                field.getInners().stream().forEach(b -> { if (outComingLinks.containsKey(b)) outComingLinks.get(b).clear();});
            }
        }
        return false;
    }

/*    private double minDistance(Point p) {
        return baseField.getBases().stream().mapToDouble(b -> GeoUtils.getDistance(p.getLatlng(), b.getLatlng())).min().getAsDouble();
    }*/

    public boolean run() {
        return triangulateFields(unprocessedPoints);
    }

    public boolean checkPoint(Point p, int amount) {
        return outComingLinks.computeIfAbsent(p, v-> new HashSet<>()).size() <= amount;
    }

    public Field getBaseField() {
        return baseFields.stream().findAny().get();
    }

    public Field searchField(Point p, Field biggerField) {
        if (!biggerField.getInners().contains(p)) {
            return null;
        }
        Point innerPoint = biggerField.getInnerPoint();
        if (innerPoint == null || innerPoint.equals(p)) {
            return biggerField;
        }

        return biggerField.getSmallerFields().stream().map(f -> searchField(p, f)).filter(f -> f != null).findAny().get();
    }

    public Field searchField(Point p, Collection<Field> fields) {
        return fields.stream().map(f -> searchField(p, f)).filter(f -> f != null).findAny().get();
    }


}
