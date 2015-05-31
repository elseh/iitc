package iitc.triangulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by epavlova on 5/29/2015.
 */
public class GeoUtils {
    public static boolean isPointInsideField(Point p, Triple<Point> triple) {
        return triple.split().stream().map(new Function<Pair<Point>, Double>() {
            @Override
            public Double apply(Pair<Point> pointPair) {
                return Math.signum(mult(p, pointPair));
            }
        }).collect(Collectors.toSet()).size() == 1;
    }

    public static double mult(Point c, Pair<Point> ends) {
        return (
                (c.getLat() - ends.v1.getLat()) * (c.getLng() - ends.v2.getLng())
              - (c.getLng() - ends.v1.getLng()) * (c.getLat() - ends.v2.getLat())
        );
    }

    public static List<Point> group(Point ... points) {
        return Arrays.asList(points);
    }

    public static class Pair<T> {
        public final T v1, v2;

        private Pair(T v1, T v2) {
            this.v1 = v1;
            this.v2 = v2;
        }

        public static <T> Pair<T> of(T v1, T v2) {
            return new Pair<>(v1, v2);
        }

        public Pair<T> reverse() {
            return of(v2, v1);
        }
    }

    public static class Triple<T> {
        public final T v1, v2, v3;

        private Triple(T v1, T v2, T v3) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
        }

        public static <T> Triple<T> of(T v1, T v2, T v3) {
            return new Triple<>(v1, v2, v3);
        }

        public static <T> Triple<T> of(T v1, Pair<T> pair) {
            return of(v1, pair.v1, pair.v2);
        }


        public List<Pair<T>> split() {
            return Arrays.asList(Pair.of(v1, v2), Pair.of(v2, v3), Pair.of(v3, v1));
        }
    }
}
