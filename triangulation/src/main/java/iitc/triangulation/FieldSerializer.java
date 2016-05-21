package iitc.triangulation;

import com.google.gson.Gson;
import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.LatLngs;
import iitc.triangulation.shapes.Link;
import iitc.triangulation.shapes.Triple;
import org.apache.commons.lang.StringUtils;
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
    private Map<Point, Set<Point>> frame;

    public void insertFrame(Map<Point, Set<Point>> frame) {
        frame.entrySet()
                .stream()
                .forEach((e) -> {
                    linksOrder
                            .computeIfAbsent(e.getKey(), a -> new ArrayList<>())
                            .addAll(e.getValue());
                });
        this.frame = frame;
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
        SVGSerializer serializer = new SVGSerializer(linksOrder, pointsOrder, frame);
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
        int i = 0;
        VelocityContext context = new VelocityContext();
        context.put("linksMap", linksMap);
        context.put("fields", fieldList);
        context.put("links", lineList);
        context.put("points", pointsOrder);
        context.put("linksOrder", linksOrder);
        context.put("path", new Drawing("polyline", pointsOrder.toArray(new Point[pointsOrder.size()])).setColor("pink"));
        context.put("length", length(pointsOrder));
        context.put("numberTool", new NumberTool());
        context.put("util", new Utils());
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

    public String serializeMaxField() {
        // Brunnen; https://www.ingress.com/intel?ll=52.357459,9.660858&z=17&pll=52.357459,9.660858
        // Link, Agent, MapNumOrigin, OriginName, MapNumDestination, DestinationName
        List<Point> points = new ArrayList<>();
        HashMap<Point, Integer> pointsIndex = new HashMap<>();
        HashMap<Point, Integer> keys = new HashMap<>();
        for (Point p : linksOrder.keySet()) {
            points.add(p);
        }

        int i = 20;
        List<NumberedLink> links = new ArrayList<>();
        for (Point from: pointsOrder) {
            int stage = i++;
            pointsIndex.putIfAbsent(from, stage);
            for (Point to : linksOrder.get(from)) {
                pointsIndex.putIfAbsent(to, stage);
                links.add(new NumberedLink(from, to, i++));
                keys.putIfAbsent(to, 0);
                keys.compute(to, (p, value) -> value+1);
            }
        }

        VelocityContext context = new VelocityContext();
        context.put("links", links);
        context.put("points", points);
        context.put("index", pointsIndex);
        context.put("util", new Utils());
        context.put("keys", keys);
        Template template = null;
        try {
            StringWriter sw = new StringWriter();
            template = Velocity.getTemplate("templates/result-maxField.txt.vm");
            template.merge(context, sw);
            return sw.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static class Task {
        /*
        var task = {
        id: -1,
        guid: guid,
        title: portal.options.data.title,
        description: "",
        actionText: "",
        status: 0,
        portalUrl: "https://www.ingress.com" + "/intel?ll=" + lat + "," + lng + "&z=17&pll=" + lat + "," + lng + "",
        lat: lat,
        lon: lng,
        action: action,
        flowOrder: window.plugin.iclight.flowOrder,
        executorTeam: executorTeam,
        beginTime: 0,
        endTime: 0,
        type: 0,
        team: 0,
        linkTitle: "",
        linkLat: 0,
        linkLon: 0,
        linkUrl: "",
        remoteCommands: "",
        childTasks: {}
    };
        * */
    }

    public static class NumberedLink {
        public NumberedLink(Point from, Point to, int linkNumber) {
            this.from = from;
            this.to = to;
            this.linkNumber = linkNumber;
        }

        Point from,to;
        int linkNumber;

        public Point getFrom() {
            return from;
        }

        public Point getTo() {
            return to;
        }

        public int getLinkNumber() {
            return linkNumber;
        }
    }

    public static class  Utils {
        public String toURL(Point p) {
            return "https://www.ingress.com/intel?ll={0},{1}&z=17&pll={0},{1}"
                    .replaceAll("[{]0[}]", p.latlng.getLat() + "")
                    .replaceAll("[{]1[}]", p.latlng.getLng() + "");
        }

        public double length(Point p1, Point p2) {
            return DeployOrder.length(p1, p2);
        }
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

        public Drawing setColor(String color) {
            this.color = color;
            return this;
        }
    }

}
