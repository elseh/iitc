package iitc.triangulation.shapes;

import iitc.triangulation.GeoUtils;
import iitc.triangulation.Point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by epavlova on 5/29/2015.
 */
public class Field {
    private Triple<Point> bases;
    private List<Point> inner;
    private Triple<Field> smallerFields;
    private Point innerPoint;


    public Field(Triple<Point> bases, List<Point> all) {
        this.bases = bases;
        inner = all.stream().filter(p -> GeoUtils.isPointInsideField(p, bases)).collect(Collectors.toList());
    }

    public List<Point> getInners() {
        return inner;
    }

    public Triple<Point> getBases() {
        return bases;
    }

    public Triple<Field> getSmallerFields() {
        return smallerFields;
    }

    public void insertSmallerFields(Point p) {
        List<Pair<Point>> pairs = bases.split();
        smallerFields = Triple.of(
                new Field(Triple.of(p, pairs.get(0)), inner),
                new Field(Triple.of(p, pairs.get(1)), inner),
                new Field(Triple.of(p, pairs.get(2)), inner)
        );
        innerPoint = p;
    }

    private Map<Point, List<Set<Point>>> interned = new HashMap<>();

    public List<Set<Point>> smallerTriangles(Point p) {
        return interned.computeIfAbsent(p, point -> bases.split()
                .stream()
                .map(pair -> Triple.of(p, pair).set())
                .collect(Collectors.toList()));
    }

    public void resetSmallerFields() {
        innerPoint = null;
        smallerFields = null;
    }

    public Point getInnerPoint() {
        return innerPoint;
    }
}
