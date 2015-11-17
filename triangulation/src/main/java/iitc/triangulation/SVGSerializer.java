package iitc.triangulation;

import iitc.triangulation.shapes.LatLngs;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by epavlova on 12-Nov-15.
 */
public class SVGSerializer {
    private Map<Point, List<Point>> linksOrder;
    private List<Point> pointOrder;

    private Set<Point> all;

    private double dx;
    private double dy;
    private double width;
    private double height;

    public SVGSerializer(Map<Point, List<Point>> linksOrder, List<Point> pointOrder) {
        this.linksOrder = linksOrder;
        this.pointOrder = pointOrder;
        width = pointOrder.stream().map(p -> p.latlng.getLat()).max(Comparator.<Double>naturalOrder()).get() -
                pointOrder.stream().map(p -> p.latlng.getLat()).min(Comparator.<Double>naturalOrder()).get();
        height = pointOrder.stream().map(p -> p.latlng.getLng()).max(Comparator.<Double>naturalOrder()).get() -
                 pointOrder.stream().map(p -> p.latlng.getLng()).min(Comparator.<Double>naturalOrder()).get();
        dx = pointOrder.stream().map(p -> p.latlng.getLat()).min(Comparator.<Double>naturalOrder()).get();
        dy = pointOrder.stream().map(p -> p.latlng.getLng()).min(Comparator.<Double>naturalOrder()).get();
        all = Stream.concat(pointOrder.stream(), linksOrder.values().stream().flatMap(Collection::stream))
                .collect(Collectors.toSet());
    }

    public LatLngs transform(Point p) {
        return new LatLngs((p.getLatlng().getLat() - dx) * 90 / width + 5, (p.getLatlng().getLng() - dy) * 90 / height + 5);
    }

    public String makePoints() {
        return all.stream()
                .map(this::transform)
                .map(l -> String.format("<circle cx=\"%f%%\" cy=\"%f%%\"  r=\"2\"/>", l.getLat(), l.getLng()))
                .collect(Collectors.joining("\n"));
    }

    public String makeLines() {
        String result = "";
        int i = 0;
        for (Point p : pointOrder) {
            LatLngs local = transform(p);
            String style = "st" + i;
            result += linksOrder.get(p)
                    .stream()
                    .map(this::transform)
                    .map(l -> makeLine(l, local, style))
                    .collect(Collectors.joining("\n"));
            i++;
        }

        for (int j = 1; j < pointOrder.size(); j++) {
            LatLngs l1 = transform(pointOrder.get(j - 1));
            LatLngs l2 = transform(pointOrder.get(j));
            result += "\n" + makeLine(l1, l2, "path");
        }
        result += "\n";

        return result;
    }

    private String makeLine(LatLngs l1, LatLngs l2, String path) {
        return String.format("<line x1=\"%f%%\" y1=\"%f%%\" x2=\"%f%%\" y2=\"%f%%\" class=\"%s\"/>",
                l1.getLat(), l1.getLng(), l2.getLat(), l2.getLng(), path);
    }

    public String makeStyles() {
        String result = IntStream.range(0, pointOrder.size())
                .mapToObj(i -> String.format(".st%d", i))
                .collect(Collectors.joining(", ")) + " {display:none;}";

        result += IntStream.range(0, pointOrder.size())
                .mapToObj(i -> String.format(".active%d .st%d", i, i))
                .collect(Collectors.joining(", ")) + " {display:initial;}";

        return result;
    }
}
