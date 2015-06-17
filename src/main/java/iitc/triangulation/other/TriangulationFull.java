package iitc.triangulation.other;

import iitc.triangulation.Point;
import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.Triple;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        values.add(Description.skipAll(set, field.getInners().size()));
        allDescriptions.put(set, values);
        return values;
    }

    public Set<Description> calculateFields(Set<Set<Point>> bases) {
        Set<Point> baseDescriptionSet = bases.stream().flatMap(Collection::stream).collect(Collectors.toSet());
        Description bDescription = Description.makeEmptyBase(baseDescriptionSet);
        return sumFields(bases, bDescription);
    }

    private Set<Description> sumFields(Field f, Point inner) {
        return sumFields(f.getBases().split()
                .stream()
                .map(p -> Triple.of(inner, p).set())
                .collect(Collectors.toSet()),
                Description.makeBase(f.getBases().set()));
    }

    private Set<Description> sumFields(Set<Set<Point>> bases, Description baseDescription) {
        Set<Description> base = new HashSet<>();
        Set<Point> pointSet = baseDescription.getLinkAmount().keySet();
        base.add(baseDescription);
        bases
                .stream()
                .map(this::calculateField).forEach(
                set -> {
                    Collection<Description> values = base.stream()
                            .flatMap(element -> set
                                    .stream()
                                    .map(e1 -> element.insert(e1)))
                            .filter(this::goodDescription)
                            .map(d -> Description.reduce(d, pointSet))

                            .collect(Collectors.toMap(Description::getLinkAmount, a -> a, Description::min))
                            .values();
                    base.clear();
                    base.addAll(values);
                }
        );
        return base;
    }

    private boolean goodDescription(Description d) {
        return !d.getLinkAmount().values().stream().filter(i -> i > 8).findFirst().isPresent();
    }

    public void restore(Description d, Field f) {
        Set<Point> set = d.getLinkAmount().keySet();

        if (d.getSkipAmount() == f.getInners().size()) {
            return;
        }
        if (!calculateField(set).contains(d)) {
            return;
        }

        Set<Point> allPoints = d.getSumOf().stream().flatMap(s -> s.getLinkAmount().keySet().stream()).collect(Collectors.toSet());
        allPoints.removeAll(d.getLinkAmount().keySet());
        Point p = allPoints.stream().findFirst().get();
        f.insertSmallerFields(p);
        Map<Set<Point>, Description> small = d.getSumOf().stream().collect(Collectors.toMap(desc -> desc.getLinkAmount().keySet(), desc -> desc));
        f.getSmallerFields().stream().forEach(sm -> restore(small.get(sm.getBases().set()), sm));
    }

    public void restore(Description d, List<Field> fields) {
        Map<Set<Point>, Description> small = d.getSumOf().stream().collect(Collectors.toMap(desc -> desc.getLinkAmount().keySet(), desc -> desc));
        fields.stream().forEach(sm -> restore(small.get(sm.getBases().set()), sm));
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
