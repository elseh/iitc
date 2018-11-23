package iitc.triangulation.other;

import iitc.triangulation.Point;
import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.Triple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author epavlova
 * @version 06.09.2018
 */
public class AllFields {
    private SingleComputer<Set<Point>, Field> allFields = new SingleComputer<>();
    private Map<Integer, List<Set<Point>>> order = new HashMap<>();

    public AllFields() {
    }

    public Field get(Set<Point> set) {
        return allFields.get(set);
    }


    private Field onAbsent(Set<Point> set, List<Point> all) {
        Point[] points = set.toArray(new Point[3]);
        Triple<Point> t = Triple.of(points[0], points[1], points[2]);
        Field field = new Field(t, all);
        order.computeIfAbsent(field.getInners().size(), i -> new ArrayList<>()).add(set);
        return field;
    }

    public int size() {
        return allFields.size();
    }


    public void pushBases(Set<Triple<Point>> bases, List<Point> all) {
        bases.forEach(b -> buildAllFields(b.set(), all));
    }

    private void buildAllFields(Set<Point> set, List<Point> all) {
        allFields
                .computeOnce(set, s -> onAbsent(set, all))
                .ifPresent(f -> {
                    buildNewFields(f);
                });
    }

    public Map<Integer, List<Set<Point>>> getOrder() {
        return order;
    }

    private void buildNewFields(Field field) {
        field.getInners().stream()
                .map(field::smallerTriangles)
                .flatMap(Collection::stream)
                .forEach(set -> buildAllFields(set, field.getInners()));
    }
}
