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
        if (field.getInners().size() == 0) {
            values.add(Description.skipAll(set, field.getInners().size()));
        }
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
                            .flatMap(element -> set.stream().map(element1 -> Description.sum(element, element1)).peek(p -> System.out.print(("[5, 3, 3]".equals("" + p) ? "!!!!" + element: "."))))
                            .filter(this::goodDescription)
                            .map(d -> Description.reduce(d, pointSet))
                            .collect(Collectors.toMap(Description::getLinkAmount, a -> a, Description::min)).values();
                    base.clear();
                    base.addAll(values);
                }
        );
        return base;
    }

    private boolean goodDescription(Description d) {
        return !d.getLinkAmount().values().stream().filter(i -> i > 8).findFirst().isPresent();
    }

    private boolean fullEquals(Description a, Description b) {
        return a.equals(b) && a.getSkipAmount() == b.getSkipAmount();
    }
    public void restore(Description d, Field f) {
        Set<Point> set = d.getLinkAmount().keySet();
        System.out.println("restoring for " + d + " | " + set);

        if (d.getSkipAmount() == f.getInners().size()) {
            System.out.println("skipping");
            return;
        }
        if (!calculateField(set).contains(d)) {
            return;
        }

        for (Point p : f.getInners()) {
            //Set<Description> descriptions = sumFields(f, p);
            /*boolean present = descriptions
                    .stream()
                    .filter(desc -> desc.equals(d) && d.getSkipAmount() == desc.getSkipAmount())
                    .findAny().isPresent();
            if (present) {*/
                f.insertSmallerFields(p);
                List<Field> fieldList = f.getSmallerFields().stream().collect(Collectors.toList());
                List<Set<Point>> setList = fieldList.stream().map(fi -> fi.getBases().set()).collect(Collectors.toList());
                Description partialSum = Description.makeBase(set);
                System.out.println("partial: " + partialSum);
                System.out.println(setList);
                Optional<List<Description>> split = split(d, partialSum, setList, 0);
                if (split.isPresent()) {
                    IntStream
                            .range(0, fieldList.size())
                            .forEach(i -> restore(split.get().get(i), fieldList.get(i)));
                    return;
                }
            /*}*/
        }
        System.out.println("unable: " + d);
    }

    public void restore(Description d, List<Field> fields) {
        Optional<List<Description>> split = split(
                d,
                Description.makeEmptyBase(d.getLinkAmount().keySet()),
                fields.stream().map(f -> f.getBases().set()).collect(Collectors.toList()),
                0);
        if (split.isPresent()) {
            IntStream
                    .range(0, fields.size())
                    .forEach(i -> restore(split.get().get(i), fields.get(i)));
        }
    }

    private Optional<List<Description>> split(Description d, Description partialSum, List<Set<Point>> bases, int pos) {
        if (!goodDescription(partialSum)) {
            return Optional.empty();
        }
        /*if (partialSum.getSkipAmount() > d.getSkipAmount()) {
            return Optional.empty();
        }*/

        //System.out.println("!" + pos + ": " + partialSum + " -> " + d);

        //System.out.println("pos " + pos  + " " + partialSum);
        if (pos == bases.size()) {
            Description sum = Description.reduce(partialSum, d.getLinkAmount().keySet());

            //boolean b = sum.equals(d) && sum.getSkipAmount() == d.getSkipAmount();
            boolean b = fullEquals(d, sum);
            if (b) {
                System.out.println("!!" + pos + ": " + partialSum + " -> " + d + ": " + b);
            }
            return b ? Optional.of(new ArrayList<>()): Optional.<List<Description>>empty() ;
        }
        for (Description description : allDescriptions.get(bases.get(pos))) {
            Optional<List<Description>> split = split(d, Description.sum(partialSum, description), bases, pos + 1);
            if (split.isPresent()) {
                split.get().add(0, description);
                System.out.println(pos + ": " +  split.get() +" + " + partialSum + " -> " + d);
                return split;
            }
        }
        return Optional.empty();
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
