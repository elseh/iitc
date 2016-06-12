package iitc.triangulation;

import iitc.triangulation.aspect.HasValues;
import iitc.triangulation.aspect.Value;
import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.KeysPriorities;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static iitc.triangulation.DeployOrder.length;
import static java.util.Collections.emptyList;

/**
 * @author epavlova
 * @version 21.05.2016
 */
@HasValues
public class OtherSerialization extends AbstractSerializer {
    private Map<Point, List<Point>> links = new HashMap<>();
    private List<Point> order = new ArrayList<>();
    //private List<Point> shortOrder = new ArrayList<>();
    private Set<Point> outerPoints = new HashSet<>();
    @Value("other.maxKeys:8") private static int maxKeys;
    @Value("other.maxLinks:3") private static int maxLinks;

    @Value("other.profile:ALL_KEYS") public static KeysPriorities priorities1;

    protected KeysPriorities priorities;

    public OtherSerialization(KeysPriorities priorities) {
        this.priorities = priorities;
    }

    private Map<Point, Point> emptyPoints = new HashMap<>();

    @Override
    protected void onInsertField(Field field) {
        field.getBases().split()
                .stream()
                .forEach(p -> addLink(p.v1, p.v2));
        outerPoints.addAll(field.getBases().set());
    }

    @Override
    protected void onSplitField(Field field, Point innerPoint) {
        field.getBases().stream().forEach(p1 -> addLink(innerPoint, p1));
    }

    private void addLink(Point p1, Point p2) {
        links.computeIfAbsent(p1, p -> new ArrayList<>()).add(p2);
        links.computeIfAbsent(p2, p -> new ArrayList<>()).add(p1);
    }

    private void removeLink(Point p1, Point p2) {
        links.computeIfPresent(p1, (p, s) -> {
            s.remove(p2);
            return s.isEmpty() ? null : s;
        });
        links.computeIfPresent(p2, (p, s) -> {
            s.remove(p1);
            return s.isEmpty() ? null : s;
        });
    }

    private boolean chooseNextPoint() {
        if (outerPoints.isEmpty()) return false;
        Point point =
                outerPoints.stream()
                .min(Comparator
                                .<Point, Boolean>comparing(p -> links.getOrDefault(p, emptyList()).size() > maxLinks)
                                .<Integer>thenComparing(this::keyCheck)
                                .<Point>thenComparingDouble(p -> order.isEmpty() ? 0 : length(p, order.get(0)))
                ).get();

        order.add(0,point);

        List<Point> linked = new ArrayList<>(Optional.ofNullable(links.get(point)).orElse(emptyList()));
        linked.stream().forEach(
                p -> {
                    removeLink(point, p);
                    requiredKeys.compute(p, (p1, i) -> Optional.ofNullable(i).orElse(0)+1);
                }
        );
        linksOrder.put(point, linked);
        requiredKeys.computeIfAbsent(point, p -> 0);
        outerPoints.addAll(linked);
        outerPoints.remove(point);

        List<Point> naked = outerPoints.stream()
                .filter(p -> links.getOrDefault(p, emptyList()).size() == 0)
                .collect(Collectors.toList());
        outerPoints.removeAll(naked);
        naked.forEach(p -> {
            emptyPoints.put(p, point);
            linksOrder.put(p, emptyList());
        });
        return true;
    }

    private Integer keyCheck(Point p) {
        int keysFor = keysStorage.keysFor(p);
        int keys = requiredKeys.getOrDefault(p, 0);
        int difference = keys - keysFor;
        if (difference > maxKeys) return priorities.muchFarm;
        if (difference == maxKeys) return priorities.maxFarm;
        if (difference > 0) return priorities.needFarm;
        if (difference == 0) return priorities.allKeys;
        if (difference < 0) return priorities.haveKeys;

        return keysFor - keys;
    }

    public double process() {
        while (chooseNextPoint()) {
            // do nothing
        }

        for (int i = 0; i < 2; i++) {
            emptyPoints.forEach(this::insertPointBefore);
        }
        //requiredKeys.keySet().stream().
        return length(order);
    }

    private void insertPointBefore(Point empty, Point last) {
        order.remove(empty);
        Integer position = IntStream.range(0, order.indexOf(last) + 1)
                .boxed().min(Comparator.comparing(i -> diff(i, empty))).get();
        order.add(position, empty);
    }

    private double diff(int i, Point between) {
        return i == 0? length(order.get(i), between) :
                (length(Arrays.asList(order.get(i), between, order.get(i-1))) - length(order.get(i-1) , order.get(i)));
    }

    @Override
    protected List<Point> getPointsOrder() {
        return order;
    }

    public boolean baseCheck() {
        return outerPoints.stream()
                .filter(p -> links.get(p).size() <= maxLinks)
                .findAny().isPresent();
    }
}
