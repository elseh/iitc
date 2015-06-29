package iitc.triangulation;

import iitc.triangulation.shapes.Triple;
import iitc.triangulation.shapes.Field;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sigrlinn on 31.05.2015.
 */
public class Triangulation {
    private Map<Point, Set<Point>> outComingLinks = new HashMap<>();
    private List<Point> unprocessedPoints = new ArrayList<>();
    private Set<Field> baseFields;

    public Triangulation(List<Point> allPoints, Set<Triple<Point>> baseTriangles, Map<Point, Set<Point>> outLinks) {
        baseFields = baseTriangles
                .stream()
                .map(t -> new Field(t, allPoints))
                .collect(Collectors.toSet());
        outComingLinks.putAll(outLinks);
        unprocessedPoints = baseFields
                .stream()
                .flatMap(f -> f.getInners().stream())
                .collect(Collectors.toList());
    }

    public boolean triangulateFields(List<Point> points) {
        if (points.isEmpty()) return true;
        List<Point> unprocessed = new ArrayList<>(points);

        for (Point p : points) {
            unprocessed.remove(p);
            Field field = searchField(p, baseFields);
            if (field.getBases()
                    .stream()
                    .anyMatch(b -> !checkPoint(b, 7))) {
                return false;
            }
            field.insertSmallerFields(p);
            field.getBases()
                    .stream()
                    .forEach(b -> outComingLinks.computeIfAbsent(b, v -> new HashSet<>()).add(p));
            if (triangulateFields(unprocessed)) {
                return true;
            } else {
                unprocessed.add(p);
                field.resetSmallerFields();
                field.getBases()
                        .stream()
                        .forEach(b -> outComingLinks.get(b).remove(p));
            }
        }
        return false;
    }

    private double minDistance(Point p) {
        return getBaseField().getBases()
                .stream()
                .mapToDouble(b -> GeoUtils.getDistance(p.getLatlng(), b.getLatlng()))
                .min()
                .getAsDouble();
    }

    public boolean run() {
        unprocessedPoints.sort(Comparator.comparing(this::minDistance));
        return triangulateFields(unprocessedPoints);
    }

    public boolean checkPoint(Point p, int amount) {
        return outComingLinks.computeIfAbsent(p, v-> new HashSet<>()).size() <= amount;
    }

    public Field getBaseField() {
        return baseFields.stream().findAny().get();
    }

    public Set<Field> getBaseFields() {
        return baseFields;
    }

    public Field searchField(Point p, Field biggerField) {
        if (!biggerField.getInners().contains(p)) {
            return null;
        }
        Point innerPoint = biggerField.getInnerPoint();
        if (innerPoint == null || innerPoint.equals(p)) {
            return biggerField;
        }

        return biggerField.getSmallerFields()
                .stream()
                .map(f -> searchField(p, f))
                .filter(f -> f != null)
                .findAny()
                .get();
    }

    public Field searchField(Point p, Collection<Field> fields) {
        return fields
                .stream()
                .map(f -> searchField(p, f))
                .filter(f -> f != null)
                .findAny()
                .get();
    }
}
