package iitc.triangulation.other;

import iitc.triangulation.Point;
import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.Triple;

import java.util.List;
import java.util.Set;

import static iitc.triangulation.GeoUtils.getArea;

/**
 * Created by Sigrlinn on 16.06.2015.
 */
public class TriangulationMax {
    private List<Point> allPoints;

    public TriangulationMax(List<Point> allPoints) {
        this.allPoints = allPoints;
    }


    private Point bestPoint(Field field) {
        double bestWeight = -1;
        Point bestPoint = null;
        for (Point point : field.getInners()) {
            field.insertSmallerFields(point);
            double sum = field.getSmallerFields().stream()
                    .mapToDouble(f -> f.getInners().size() * getArea(f.getBases().simplify(Point::getLatlng)))
                    .sum();
            if (sum > bestWeight) {
                bestWeight = sum;
                bestPoint = point;
            }
        }
        return bestPoint;
    }

    private void splitField(Field field) {
        if (field.getInners().size() == 0) return;
        Point best = bestPoint(field);
        if (best != null) {
            field.insertSmallerFields(best);
            field.getSmallerFields().stream().forEach(this::splitField);
        }
    }

    public Field analyseSingleField(Set<Point> set) {
        Field field = get(set);
        splitField(field);
        return field;
    }

    private Field get(Set<Point> set) {
        Point[] points = set.toArray(new Point[3]);
        return new Field(Triple.of(points[0], points[1], points[2]), allPoints);
    }


}
