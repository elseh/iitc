package iitc.triangulation;

import com.google.gson.Gson;
import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.Triple;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.generic.NumberTool;

import java.io.StringWriter;
import java.util.*;

import static iitc.triangulation.DeployOrder.*;

/**
 * Created by Sigrlinn on 31.05.2015.
 */
public class FieldSerializer extends AbstractSerializer {

    protected List<Point> pointsOrder = new ArrayList<>();


    private Map<Point, Set<Point>> frame;

    public void insertFrame(Map<Point, Set<Point>> frame) {
        frame.entrySet()
                .stream()
                .forEach((e) -> {
                    linksOrder
                            .computeIfAbsent(e.getKey(), a -> new ArrayList<>())
                            .addAll(e.getValue());

                    e.getValue().stream().forEach(p ->
                            requiredKeys.compute(p, (p1, i) -> Optional.ofNullable(i).orElse(0)+1)
                    );
                    requiredKeys.computeIfAbsent(e.getKey(), p -> 0);

                });
        this.frame = frame;

    }

    @Override
    protected void onInsertField(Field field) {
    }

    @Override
    protected void onSplitField(Field field, Point innerPoint) {
        Triple<Point> bases = field.getBases();
        bases.stream().forEach(v -> linksOrder.computeIfAbsent(v, a-> new ArrayList<>()).add(innerPoint));
        linksOrder.computeIfAbsent(innerPoint, a-> new ArrayList<>());
        bases.stream().forEach(v -> lineList.add(new Drawing("polyline", innerPoint, v)));
        requiredKeys.put(innerPoint, 3);
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

    @Override
    protected List<Point> getPointsOrder() {
        return pointsOrder;
    }
}
