package iitc.triangulation;

import com.google.gson.Gson;
import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.LatLngs;
import iitc.triangulation.shapes.Triple;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Sigrlinn on 31.05.2015.
 */
public class FieldSerializer {
    private List<Drawing> fieldList = new ArrayList<>();
    private List<Drawing> lineList = new ArrayList<>();
    private Map<Point, Integer> counters = new HashMap<>();

    public void insertField(Field field) {
        Triple<Point> bases = field.getBases();
        bases.stream().forEach(v -> counters.put(v, counters.getOrDefault(v, 0) + 2));
        bases.split().stream().forEach(v -> lineList.add(new Drawing("polyline", v.v1, v.v2)));
        splitField(field);
    }

    private void splitField(Field field) {
        Triple<Point> bases = field.getBases();
        fieldList.add(new Drawing("polygon", bases.v1, bases.v2, bases.v3));
        if (field.getInners().isEmpty()) {
            return;
        }
        Point innerPoint = field.getInnerPoint();
        bases.stream().forEach(v -> lineList.add(new Drawing("polyline", innerPoint, v)));
        bases.stream().forEach(v -> counters.put(v, counters.getOrDefault(v, 0) + 1));
        counters.put(innerPoint, counters.getOrDefault(innerPoint, 0) + 3);

        field.getSmallerFields().stream().forEach(v -> splitField(v));
    }

    public String serialize() {
        Gson gson = new Gson();
        for (Point key : counters.keySet()) {
            System.out.println(key.getTitle() + " : " + counters.get(key));
        }
        return gson.toJson(fieldList) + "\n" + gson.toJson(lineList) + "\n";
    }

    private class Drawing {
        private String type = "polyline";
        private List<LatLngs> latLngs;
        private String color = "green";

        public Drawing(String type, Point ... points) {
            this.type = type;
            this.latLngs = Arrays.asList(points).stream().map(Point::getLatlng).collect(Collectors.toList());
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
