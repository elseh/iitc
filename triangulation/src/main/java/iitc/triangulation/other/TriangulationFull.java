package iitc.triangulation.other;

import iitc.triangulation.Point;
import iitc.triangulation.shapes.Triple;
import iitc.triangulation.shapes.Field;

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

    public Set<Description> analyseSingleField(Set<Point> set) {
        Field field = get(set);

        if (allDescriptions.containsKey(set)) {
            return allDescriptions.get(set);
        }

        Set<Description> values = new HashSet<>(field.getInners()
                .stream()
                .flatMap(p -> sumFields(field, p).stream())
                .filter(this::goodDescription)
                .collect(Collectors.toMap(Description::getLinkAmount, a -> a, Description::min)).values());
        if (field.getInners().size() < 1) {
            values.add(Description.skipAll(set));
        }
        allDescriptions.put(set, values);
        if (allDescriptions.size() % 100 == 0) {
            System.out.println("size: " + allDescriptions.size() + " " + new Date());
        }
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
                .map(this::analyseSingleField).forEach(
                set -> {
                    Collection<Description> values = base.stream()
                            .flatMap(element -> set
                                    .stream()
                                    .map(e1 -> element.insert(e1)))
                            .filter(this::goodDescription)
                            .collect(Collectors.toMap(
                                            Description::getLinkAmount,
                                            a -> a,
                                            Description::min
                                    )
                            )
                                    .values();
                    base.clear();
                    base.addAll(values);
                }
        );
        return new HashSet<>(base
                .stream()
                .map(d -> Description.reduce(d, pointSet))
                .collect(Collectors.toMap(Description::getLinkAmount, a -> a, Description::min))
                .values());
    }

    private boolean goodDescription(Description d) {
        return !d.getLinkAmount().entrySet()
                .stream()
                .filter(e -> e.getValue() > e.getKey().getMaxLinks())
                .findFirst().isPresent() && d.checkSumInTheInnerPoint();
    }

    public void restore(Description d, Field f) {
        Set<Point> set = d.getLinkAmount().keySet();

        if (0 == f.getInners().size()) {
            return;
        }
        if (!analyseSingleField(set).contains(d)) {
            return;
        }
        Point p = d.getSumOf()
                .stream()
                .flatMap(s -> s.getLinkAmount().keySet().stream())
                .filter(s -> !set.contains(s))
                .findAny().get();
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
