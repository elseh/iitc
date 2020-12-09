package iitc.triangulation.old;

import iitc.triangulation.Point;
import iitc.triangulation.keys.KeysStorage;
import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Created by epavlova on 6/10/2015.
 */
public class ParallelTriangulation {
    private static Logger log = LogManager.getLogger(ParallelTriangulation.class);

    private Map<Point, Set<Point>> outComingLinks = new HashMap<>();
    private List<Field> baseFields;
    private long counter = 0;
    private int goodnesTreshold;
    public ParallelTriangulation(List<Point> allPoints, Set<Triple<Point>> baseTriangles, Map<Point, Set<Point>> outLinks, int goodnesTreshold) {
        this.goodnesTreshold = goodnesTreshold;
        baseFields = baseTriangles
                .stream()
                .map(t -> new Field(t, allPoints))
                .collect(toList());
        outComingLinks.putAll(outLinks);
    }

    private Stream<Field> extractOpenFields(Field field) {
        if (field.getInnerPoint() != null) {
            return field.getSmallerFields()
                    .stream()
                    .flatMap(this::extractOpenFields);
        }
        if (field.getInners().size() == 0) {
            return Stream.empty();
        }
        return Stream.of(field);
    }

    private List<Field> extractOpenFields() {
        return baseFields
                .stream()
                .flatMap(this::extractOpenFields)
                .collect(toList());
    }

    private List<List<Point>> extractPointsToPlace(List<Field> fields) {
        return fields
                .stream()
                .map((field) -> field.getInners()
                        .stream()
                        /*.filter(p -> calculateGoodness(field, p) < goodnesTreshold)*/
                        .sorted(Comparator.comparing((Point p) -> calculateGoodness(field, p)).reversed())
                        .collect(toList()))
                .collect(toList());
    }

    private int calculateGoodness(Field field, Point p) {
        field.insertSmallerFields(p);
        int side = field.getSmallerFields().stream().mapToInt(f -> f.getInners().size()).sorted().toArray()[1];
        field.resetSmallerFields();
        return side;

    }

    private boolean iterateOverGroups(List<Field> openFields, List<List<Point>> pointGroups, int deep) {
        if (deep == openFields.size()) {
            return triangulate();
        }
        Field openField = openFields.get(deep);
        List<Point> points = pointGroups.get(deep);
        for (Point point : points) {
            openField.insertSmallerFields(point);
            openField.getBases()
                    .stream()
                    .forEach(base -> outComingLinks.computeIfAbsent(base, p -> new HashSet<>()).add(point));
            if (iterateOverGroups(openFields, pointGroups, deep + 1)) {
                return true;
            }
            openField.resetSmallerFields();
            openField.getBases()
                    .stream()
                    .forEach(base -> outComingLinks.computeIfAbsent(base, p -> new HashSet<>()).remove(point));
        }
        return false;
    }

    public boolean triangulate() {
        counter++;
        List<Field> openFields = extractOpenFields();
        List<List<Point>> pointsToPlace = extractPointsToPlace(openFields);
        if (openFields.size() == 0) {
            log.info(counter);
            return true;
        }
        if (counter % 1000 == 0) {
            log.info("fields: {} points: {} vars: {}", openFields.size(),
                pointsToPlace.stream()
                    .mapToInt(List::size).sum(),
                pointsToPlace.stream().mapToInt(List::size).max());
        }
        Map<Point, Long> requiredLinks = openFields
                .stream()
                .flatMap(field -> field.getBases().stream())
                .collect(groupingBy(Function.<Point>identity(), counting()));

        for (Point p : requiredLinks.keySet()) {
            if (outComingLinks.computeIfAbsent(p, v -> new HashSet<>()).size() + requiredLinks.get(p) > p.getMaxLinks()) {
                return false;
            }
        }

        return iterateOverGroups(openFields, pointsToPlace, 0);
    }

    public List<Field> getBaseFields() {
        return baseFields;
    }
}
