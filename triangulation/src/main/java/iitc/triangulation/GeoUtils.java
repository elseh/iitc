package iitc.triangulation;

import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.Pair;
import iitc.triangulation.shapes.Triple;
import iitc.triangulation.shapes.LatLngs;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by epavlova on 5/29/2015.
 */
public class GeoUtils {
    public static boolean isPointInsideField(Point p, Triple<Point> triple) {
        return triple.split().stream().map(pp -> Math.signum(mult(p.getLatlng(), pp.simplify(Point::getLatlng)))).collect(Collectors.toSet()).size() == 1;
    }

    public static double mult(LatLngs c, Pair<LatLngs> ends) {
        return (
                (c.getLat() - ends.v1.getLat()) * (c.getLng() - ends.v2.getLng())
              - (c.getLng() - ends.v1.getLng()) * (c.getLat() - ends.v2.getLat())
        );
    }

    public static double getDistance(Pair<LatLngs> points) {
        return Math.pow(points.v1.getLng() - points.v2.getLng(), 2) + Math.pow(points.v1.getLat() - points.v2.getLat(), 2);
    }

    public static Double getDistance(LatLngs p1, LatLngs p2) {
        return Math.pow(p1.getLng() - p2.getLng(), 2) + Math.pow(p1.getLat() - p2.getLat(), 2);
    }

    public static LatLngs getCenter(Triple<LatLngs> points) {
        return new LatLngs(
                (points.v1.getLat() + points.v2.getLat() + points.v3.getLat())/3,
                (points.v1.getLng() + points.v2.getLng() + points.v3.getLng())/3
        );
    }

    public static double getArea(Triple<LatLngs> points) {
        //System.out.println("points: " + points.set());
        double s1 = DeployOrder.length(points.v2, points.v3);
        double s2 = DeployOrder.length(points.v3, points.v1);
        double s3 = DeployOrder.length(points.v1, points.v2);

        double p = (s1 + s2 + s3) / 2;
        //System.out.printf("%s %s %s %s %s %n", s1, s2, s3, p, p * (p - s1) * (p - s2) * (p - s3));
        return Math.sqrt(p * (p - s1) * (p - s2) * (p - s3));
    }

    public static double fieldArea(Field field) {
        return getArea(field.getBases().simplify(Point::getLatlng)) + Optional.ofNullable(field.getSmallerFields())
                .map(t -> t.stream().mapToDouble(GeoUtils::fieldArea).sum()).orElse(0d);
    }
}
