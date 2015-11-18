package iitc.triangulation;

import com.google.gson.Gson;
import iitc.triangulation.shapes.LatLngs;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.generic.NumberTool;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static iitc.triangulation.DeployOrder.length;

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
        width = pointOrder.stream().map(p -> transform(p).getLng()).max(Comparator.<Double>naturalOrder()).get() -
                pointOrder.stream().map(p -> transform(p).getLng()).min(Comparator.<Double>naturalOrder()).get();
        height = pointOrder.stream().map(p -> transform(p).getLat()).max(Comparator.<Double>naturalOrder()).get() -
                 pointOrder.stream().map(p -> transform(p).getLat()).min(Comparator.<Double>naturalOrder()).get();
        dx = pointOrder.stream().map(p -> transform(p).getLng()).min(Comparator.<Double>naturalOrder()).get();
        dy = pointOrder.stream().map(p -> transform(p).getLat()).min(Comparator.<Double>naturalOrder()).get();
        all = Stream.concat(pointOrder.stream(), linksOrder.values().stream().flatMap(Collection::stream))
                .collect(Collectors.toSet());
    }

    public LatLngs transform(Point p) {
        //return p.getLatlng();
        //return new LatLngs((p.getLatlng().getLat() - dx) * 90 / width + 5, (p.getLatlng().getLng() - dy) * 90 / height + 5);
        return new LatLngs(
                1000 * Math.log(Math.tan(p.getLatlng().getLat() * Math.PI / 180 / 2 + (Math.PI / 4))),
                1000 * p.getLatlng().getLng() * Math.PI / 180
        );
    }

    public VelocityContext makeTemplate() {
        VelocityContext context = new VelocityContext();
        context.put("points", all);
        context.put("order", pointOrder);
        context.put("first", pointOrder.get(0));
        context.put("lines", linksOrder);
        context.put("s", this);
        context.put("view", dx  + " " + 0 + " " + (width) + " " + (height));
        context.put("height", height + dy) ;
        context.put("radius", Math.min(width, height) / 300);
        return context;
    }
}
