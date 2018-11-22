package iitc.triangulation;

import com.google.gson.Gson;
import iitc.triangulation.aspect.HasValues;
import iitc.triangulation.aspect.Value;
import iitc.triangulation.keys.KeysStorage;
import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.Triple;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.generic.NumberTool;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static iitc.triangulation.DeployOrder.length;

/**
 * @author epavlova
 * @version 22.05.2016
 */
@HasValues
public abstract class AbstractSerializer {
    static {
        try {
            Velocity.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected List<Drawing> fieldList = new ArrayList<>();
    protected List<Drawing> lineList = new ArrayList<>();
    protected Map<Point, Integer> requiredKeys = new HashMap<>();

    protected Map<Point, List<Point>> linksOrder = new HashMap<>();
    protected Map<Point, Integer> emptyLinks = new HashMap<>();
    private Set<Point> allPoints = new HashSet<>();
    protected static KeysStorage keysStorage;

    @Value("other.players:1") private static int players;

    public static void setKeysStorage(KeysStorage keysStorage) {
        AbstractSerializer.keysStorage = keysStorage;
    }

    public void insertField(Field field) {
        Triple<Point> bases = field.getBases();
        bases
                .split()
                .forEach(v -> lineList.add(new Drawing("polyline", v.v1, v.v2)));
        allPoints.addAll(field.getInners());
        allPoints.addAll(field.getBases().set());
        onInsertField(field);
        splitField(field);
    }

    protected abstract void onInsertField(Field field);

    protected void splitField(Field field) {
        Triple<Point> bases = field.getBases();
        fieldList.add(new Drawing("polygon", bases.v1, bases.v2, bases.v3));
        Point innerPoint = field.getInnerPoint();
        //writeDown(deep, innerPoint);
        if (field.getInners().isEmpty()) {
            return;
        }
        //System.out.println(innerPoint + " " + field.getInners());
        if (innerPoint != null) {
            onSplitField(field, innerPoint);
            bases.stream().forEach(v -> lineList.add(new Drawing("polyline", innerPoint, v)));
            field.getSmallerFields().stream().forEach(this::splitField);
        }
    }

    protected abstract void onSplitField(Field field, Point innerPoint);



    public String serializeOldText(String type) {
        VelocityContext context = new VelocityContext();

        Map<Point, Integer> keyDiff = getKeyDiff();
        List<Point> sorted = linksOrder.keySet()
                .stream()
                .sorted(Comparator.comparing(Point::getTitle))
                .collect(Collectors.toList());
        context.put("fields", fieldList);
        context.put("links", lineList);
        context.put("numberTool", new NumberTool());
        context.put("util", new Utils());
        context.put("gson", new Gson());
        List<Point> order = getPointsOrder();
        context.put("points", order);
        context.put("path", new Drawing("polyline", order.toArray(new Point[0])).setColor("green"));
        context.put("length", length(order));
        context.put("requiredKeys", requiredKeys);
        context.put("total", allPoints.size());
        context.put("linksOrder", linksOrder);
        context.put("storage", keyDiff);
        context.put("emptyLinks", emptyLinks);
        context.put("sorted", sorted);
        Template template = null;

        try {
            StringWriter sw = new StringWriter();
            template = Velocity.getTemplate("templates/result." + type + ".vm");
            template.merge(context, sw);
            return sw.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public Map<Point, Integer> getKeyDiff() {
        return linksOrder.keySet()
                .stream()
                .collect(Collectors.toMap(
                        p -> p,
                        p -> requiredKeys.getOrDefault(p, 0) - keysStorage.keysFor(p)
                ));
    }

    public void printStatistics() {
        List<Point> order = getPointsOrder();
        Map<Point, Integer> keyDiff = getKeyDiff();

        if (this instanceof OtherSerialization) {
            System.out.println(((OtherSerialization)this).priorities);
        }
        System.out.println("  path length :   " + (int) length(order));
        System.out.print("  keys statistics:    ");
        System.out.println("" + requiredKeys.values().stream().filter(v -> v > 0).collect(Collectors.summarizingInt(v -> v)));
        System.out.print("  farm statistics:    ");
        System.out.println("" + keyDiff.values().stream().filter(v -> v > 0).collect(Collectors.summarizingInt(v -> v)));
        System.out.println("    emptyLinks: " + emptyLinks.values().stream().mapToInt(i -> i).sum());
    }

    public String serializeMaxField() {
        // Brunnen; https://www.ingress.com/intel?ll=52.357459,9.660858&z=17&pll=52.357459,9.660858
        // Link, Agent, MapNumOrigin, OriginName, MapNumDestination, DestinationName

        HashMap<Point, Integer> pointsIndex = new HashMap<>();
        HashMap<Point, Integer> keys = new HashMap<>();
        List<Point> points = new ArrayList<>(linksOrder.keySet());

        int i = 20;
        List<FieldSerializer.NumberedLink> links = new ArrayList<>();
        for (Point from: getPointsOrder()) {
            int stage = i++;
            pointsIndex.putIfAbsent(from, stage);
            for (Point to : linksOrder.get(from)) {
                pointsIndex.putIfAbsent(to, stage);
                links.add(new FieldSerializer.NumberedLink(from, to, i++));
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

    protected abstract List<Point> getPointsOrder();
}
