package iitc.triangulation.other;

import iitc.triangulation.Point;
import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.Triple;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sigrlinn on 16.06.2015.
 */
public class TriangulationFull {
    private HashMap<Set<Point>, Set<Description>> allDescriptions = new HashMap<>();
    private HashMap<Set<Point>, Field> allFields = new HashMap<>();
    private List<Point> allPoints;

    public TriangulationFull(List<Point> allPoints) {
        this.allPoints = allPoints;
    }

    public Set<Description> calculateField(Set<Point> set) {
        Field field = get(set);

        if (allDescriptions.containsKey(set)) {
            return allDescriptions.get(set);
        }

        Set<Description> values = new HashSet<>(field.getInners()
                .stream()
                .flatMap(p -> sumFields(field, p).stream())
                .filter(element -> element.getLinkAmount().values().stream().allMatch(links -> links <= 8))
                .collect(Collectors.toMap(Description::getLinkAmount, a -> a, Description::min)).values());
        values.add(Description.skipAll(field));
        allDescriptions.put(set, values);
        return values;
    }

    private Set<Description> sumFields(Field f, Point inner) {
        Set<Description> base = new HashSet<>();
        base.add(Description.makeBase(f));
        f.getBases().split()
                .stream()
                .map(p -> Triple.of(inner, p).set())
                .map(this::calculateField).forEach(
            set -> {
                Collection<Description> values = base.stream()
                        .flatMap(element -> set.stream().map(element1 -> Description.sum(element, element1)))
                        .filter(element -> element.getLinkAmount().values().stream().allMatch(links -> links <= 8))
                        .map(d -> Description.reduce(d, f))
                        .collect(Collectors.toMap(Description::getLinkAmount, a -> a, Description::min)).values();
                base.clear();
                base.addAll(values);
            }
        );
        return base;
    }

    public void restore(Description d, Field f) {
        Set<Point> set = d.getLinkAmount().keySet();

        if (d.getSkipAmount() == f.getInners().size()) {
            return;
        }
        Set<Description> dForField = allDescriptions.get(set);
        if (!dForField.contains(d)) {
            return;
        }

        for (Point p : f.getInners()) {
            Set<Description> descriptions = sumFields(f, p);
            boolean present = descriptions
                    .stream()
                    .filter(desc -> desc.equals(d) && d.getSkipAmount() == desc.getSkipAmount())
                    .findAny().isPresent();
            if (present) {
                f.insertSmallerFields(p);
                Set<Description> ds1 = allDescriptions.get(f.getSmallerFields().v1.getBases().set());
                Set<Description> ds2 = allDescriptions.get(f.getSmallerFields().v2.getBases().set());
                Set<Description> ds3 = allDescriptions.get(f.getSmallerFields().v3.getBases().set());
                for (Description d1 : ds1) {
                    for (Description d2 : ds2) {
                        for (Description d3 : ds3) {
                            Description d0 = Description.makeBase(f);
                            d0 = Description.sum(d0, d1);
                            d0 = Description.sum(d0, d2);
                            d0 = Description.sum(d0, d3);
                            if (d0.getSkipAmount() == d.getSkipAmount()
                                    && d.equals(Description.reduce(d0, f))
                                    && d0.getLinkAmount().get(p) <=8)
                            {
                                restore(d1, f.getSmallerFields().v1);
                                restore(d2, f.getSmallerFields().v2);
                                restore(d3, f.getSmallerFields().v3);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private Field get(Set<Point> set) {
        if (!allFields.containsKey(set)) {
            Point[] points = set.toArray(new Point[3]);
            Triple<Point> t = Triple.of(points[0], points[1], points[2]);
            Field f = new Field(t, allPoints);
            allFields.put(set, f);
        }
        return allFields.get(set);
    }
}
