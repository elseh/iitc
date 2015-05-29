package iitc.triangulation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by epavlova on 5/29/2015.
 */
public class Field {
    private List<Point> main;
    private List<Point> inner;

    public Field(Point p1, Point p2, Point p3, List<Point> other) {
        main = new ArrayList<>();
        main.add(p1);
        main.add(p2);
        main.add(p3);
        inner = other.stream().filter(new Predicate<Point>() {
            @Override
            public boolean test(Point point) {
                return GeoUtils.isPointInsideField(point, p1, p2, p3);
            }
        }).collect(Collectors.toList());

    }


}
