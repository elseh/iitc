package iitc.triangulation;

/**
 * Created by epavlova on 5/29/2015.
 */
public class GeoUtils {
    public static boolean isPointInsideField(Point p, Point p1, Point p2, Point p3) {
        double side1 = Math.signum(mult(p, p1, p2));
        double side2 = Math.signum(mult(p, p2, p3));
        double side3 = Math.signum(mult(p, p3, p1));
        /*if (side1 * side2 * side3 == 0) {
            System.out.println("on: " + p);
        }*/
        return (side1 == side2) && (side1 == side3);

    }

    public static double mult(Point p1, Point p2, Point c) {
        return ((c.getLat() - p1.getLat()) * (c.getLng() - p2.getLng())
                - (c.getLng() - p1.getLng()) * (c.getLat() - p2.getLat()));
    }
}
