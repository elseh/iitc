package iitc.triangulation;

import iitc.triangulation.keys.KeysStorage;
import iitc.triangulation.shapes.LatLngs;
import iitc.triangulation.shapes.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by epavlova on 6/2/2015.
 */
public class DeployOrder {
    private static Logger log = LogManager.getLogger(DeployOrder.class);

    private Map<Point, List<Point>> linksOrder;

    private Map<Point, Set<Point>> links = new HashMap<>();

    public DeployOrder(Map<Point, List<Point>> linksOrder) {
        this.linksOrder = linksOrder;
    }

    public static double length(Point a, Point b) {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(a.latlng.getLat() - b.latlng.getLat());
        double lngDiff = Math.toRadians(a.latlng.getLng() - b.latlng.getLng());
        double alpha = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(a.latlng.getLat())) * Math.cos(Math.toRadians(b.latlng.getLat())) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double beta = 2 * Math.atan2(Math.sqrt(alpha), Math.sqrt(1 - alpha));
        double distance = earthRadius * beta;
        int meterConversion = 1609;
        return distance * meterConversion;
    }

    public static double length(LatLngs a, LatLngs b) {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(a.getLat() - b.getLat());
        double lngDiff = Math.toRadians(a.getLng() - b.getLng());
        double alpha = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(a.getLat())) * Math.cos(Math.toRadians(b.getLat())) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double beta = 2 * Math.atan2(Math.sqrt(alpha), Math.sqrt(1 - alpha));
        double distance = earthRadius * beta;
        int meterConversion = 1609;
        return distance * meterConversion;
    }

    public static double length(List<Point> points) {
        double l = 0;
        for (int i = 1; i < points.size(); i++) {
            l += length(points.get(i), points.get(i-1));
        }
        return l;
    }

    private Set<Point> getAllPoints() {
        return Stream.concat(linksOrder.values().stream().flatMap(Collection::stream), linksOrder.keySet().stream())
                .distinct()
                .filter(p -> linksOrder.get(p) != null && !linksOrder.get(p).isEmpty())
                .collect(Collectors.toSet());
    }

    public List<Point> extractPointOrder() {
        Set<Point> all = getAllPoints();
        return all
                .stream()
                .map(this::extractPointOrder)
                .filter(a -> a.size() == all.size())
                .min(Comparator.comparing(DeployOrder::length)).get();
    }

    private List<Point> extractPointOrder(Point mPoint) {
        List<Point> result = new ArrayList<>();
        Set<Point> all = getAllPoints();

        movePoint(result, all, mPoint);
        Point last = mPoint;
        while (!all.isEmpty()) {
            Optional<Point> opt = searchSafe(last, all);
            if (!opt.isPresent()) {
                log.error("ops {} {} {}", result.size(), all.size(), result);
                return result;
            }
            movePoint(result, all, opt.get());
            last = opt.get();
        }
        return result;
    }

    private Optional<Point> searchSafe(Point last, Set<Point> all) {
        Map<Point, Double> distance =
                all.stream().collect(Collectors.toMap(
                        p -> p,
                        p -> length(p, last)
                ));
        return  all
                .stream()
                .filter(p -> isSafeToAdd(p, all))
                .min(Comparator.comparing(distance::get));
    }

    private void movePoint(List<Point> result, Set<Point> all, Point point) {
        result.add(point);
        all.remove(point);
        linksOrder.get(point).stream().filter(all::contains)
                .forEach(p -> {
                    links.computeIfAbsent(point, point1 -> new HashSet<>()).add(p);
                    links.computeIfAbsent(p, point1 -> new HashSet<>()).add(point);
                });
    }

    private boolean isSafeToAdd(Point p, Set<Point> all) {
        Set<Point> oneLinkCandidates = links.entrySet().stream()
                .filter(entry -> entry.getValue().contains(p))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        List<Point> affected = linksOrder.get(p);

        Stream<Triple<Point>> rStream = Stream.<Stream<Triple<Point>>>of(
                affected.stream()
                        .flatMap(a -> oneLinkCandidates.stream()
                                        .filter(b -> links.get(b).contains(a))
                                        .map(b -> Triple.of(a, b, p))
                        ),
                links.entrySet().stream()
                        .filter(entry ->affected.contains(entry.getKey()))
                        .flatMap(entry -> entry.getValue().stream()
                                        .filter(affected::contains)
                                        .map(a -> Triple.of(a, entry.getKey(), p))
                        )
        ).flatMap(s -> s);

        return rStream
                .flatMap(t -> all.stream().filter(a -> GeoUtils.isPointInsideField(a, t)))
                .distinct()
                .filter(all::contains)
                .allMatch(a -> linksOrder.get(a).isEmpty());
    }
}
