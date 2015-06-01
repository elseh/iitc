package iitc.triangulation;

import iitc.triangulation.shapes.LatLngs;
import iitc.triangulation.shapes.Pair;
import iitc.triangulation.shapes.Triple;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by epavlova on 5/29/2015.
 */
public class GeoUtils {
    public static boolean isPointInsideField(Point p, Triple<Point> triple) {
        return triple.split().stream().map(pp -> Math.signum(mult(p.getLatlng(), pp.simplify(point->point.getLatlng())))).collect(Collectors.toSet()).size() == 1;
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
}
