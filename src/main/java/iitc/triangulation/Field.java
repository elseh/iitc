package iitc.triangulation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by epavlova on 5/29/2015.
 */
public class Field {
    private GeoUtils.Triple<Point> bases;
    private List<Point> inner;
    private GeoUtils.Triple<Field> smallerFields;
    private Point innerPoint;


    public Field(GeoUtils.Triple<Point> bases, List<Point> all) {
        this.bases = bases;
        inner = all.stream().filter(new Predicate<Point>() {
            @Override
            public boolean test(Point point) {
                return GeoUtils.isPointInsideField(point, bases);
            }
        }).collect(Collectors.toList());
    }

    public List<Point> getInners() {
        return inner;
    }

    public GeoUtils.Triple<Point> getBases() {
        return bases;
    }

    public GeoUtils.Triple<Field> getSmallerFields() {
        return smallerFields;
    }

    public void insertSmallerFields(Point p) {
        List<GeoUtils.Pair<Point>> pairs = bases.split();
        smallerFields = GeoUtils.Triple.of(
                new Field(GeoUtils.Triple.of(p, pairs.get(0)), inner),
                new Field(GeoUtils.Triple.of(p, pairs.get(1)), inner),
                new Field(GeoUtils.Triple.of(p, pairs.get(2)), inner)
        );
        innerPoint = p;
    }

    public Point getInnerPoint() {
        return innerPoint;
    }
}
