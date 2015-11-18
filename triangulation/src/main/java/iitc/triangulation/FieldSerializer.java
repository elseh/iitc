package iitc.triangulation;

import com.google.gson.Gson;
import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.LatLngs;
import iitc.triangulation.shapes.Triple;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.generic.NumberTool;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static iitc.triangulation.DeployOrder.*;

/**
 * Created by Sigrlinn on 31.05.2015.
 */
public class FieldSerializer {

    static {
        try {
            Velocity.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Drawing> fieldList = new ArrayList<>();
    private List<Drawing> lineList = new ArrayList<>();
    private Map<Point, List<Point>> linksOrder = new HashMap<>();

    private Map<Point, Set<Point>> linksMap = new HashMap<>();
    private List<Point> pointsOrder;

    public void insertFrame(Map<Point, Set<Point>> frame) {
        frame.entrySet()
                .stream()
                .forEach((e) -> {
                    linksOrder
                            .computeIfAbsent(e.getKey(), a -> new ArrayList<>())
                            .addAll(e.getValue());
                });
    }

    public void insertField(Field field) {
        Triple<Point> bases = field.getBases();
        bases
                .split()
                .forEach(v -> {
                    linksMap.computeIfAbsent(v.v1, t -> new HashSet<>()).add(v.v2);
                    linksMap.computeIfAbsent(v.v2, t -> new HashSet<>()).add(v.v1);
                });

        bases
                .split()
                .stream()
                .forEach(v -> lineList.add(new Drawing("polyline", v.v1, v.v2)));
        splitField(field);
    }

    private void splitField(Field field) {
        Triple<Point> bases = field.getBases();
        fieldList.add(new Drawing("polygon", bases.v1, bases.v2, bases.v3));
        Point innerPoint = field.getInnerPoint();
        //writeDown(deep, innerPoint);
        if (field.getInners().isEmpty()) {
            return;
        }
        //System.out.println(innerPoint + " " + field.getInners());
        if (innerPoint != null) {
            bases.stream().forEach(v -> linksOrder.computeIfAbsent(v, a-> new ArrayList<>()).add(innerPoint));
            linksOrder.computeIfAbsent(innerPoint, a-> new ArrayList<>());
            bases.stream().forEach(v -> lineList.add(new Drawing("polyline", innerPoint, v)));
            bases
                    .stream()
                    .forEach(v -> {
                        linksMap.computeIfAbsent(v, t -> new HashSet<>()).add(innerPoint);
                        linksMap.computeIfAbsent(innerPoint, t -> new HashSet<>()).add(v);
                    });

            field.getSmallerFields().stream().forEach(this::splitField);
        }
    }

    public String serialiseSVG() {
        SVGSerializer serializer = new SVGSerializer(linksOrder, pointsOrder);
        VelocityContext context = serializer.makeTemplate();
        Template template = null;
        try {
            StringWriter sw = new StringWriter();
            template = Velocity.getTemplate("templates/html.vm");
            template.merge(context, sw);
            return sw.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public double preSerialize() {
        DeployOrder dOrder = new DeployOrder(linksOrder);
        pointsOrder = dOrder.extractPointOrder();
        return length(pointsOrder);
    }

    public String serialize() {

        VelocityContext context = new VelocityContext();
        context.put("linksMap", linksMap);
        context.put("fields", fieldList);
        context.put("links", lineList);
        context.put("points", pointsOrder);
        context.put("linksOrder", linksOrder);
        context.put("path", new Drawing("polyline", pointsOrder.toArray(new Point[pointsOrder.size()])));
        context.put("length", length(pointsOrder));
        context.put("numberTool", new NumberTool());
        context.put("gson", new Gson());

        Template template = null;
        try {
            StringWriter sw = new StringWriter();
            template = Velocity.getTemplate("templates/result.txt.vm");
            template.merge(context, sw);
            return sw.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static  class Drawing {
        private String type = "polyline";
        private List<LatLngs> latLngs;
        private String color = "green";

        public Drawing(String type, Point ... points) {
            this.type = type;
            this.latLngs = Arrays.asList(points)
                    .stream()
                    //.peek(x -> System.out.println(x))
                    .map(Point::getLatlng)
                    .collect(Collectors.toList());
        }

        public String getType() {
            return type;
        }

        public List<LatLngs> getLatLngs() {
            return latLngs;
        }

        public String getColor() {
            return color;
        }
    }

}
