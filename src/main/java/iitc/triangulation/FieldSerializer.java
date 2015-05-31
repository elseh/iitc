package iitc.triangulation;

import com.google.gson.Gson;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Sigrlinn on 31.05.2015.
 */
public class FieldSerializer {
    //private List<GeoUtils.Pair<Point>> links = new ArrayList<>();
    private List<Drawing> fieldList = new ArrayList<>();
    private List<Drawing> lineList = new ArrayList<>();
    private Map<Point, Integer> counters = new HashMap<>();

    public void insertField(Field field) {
        GeoUtils.Triple<Point> bases = field.getBases();
        counters.put(bases.v1, counters.getOrDefault(bases.v1, 0) + 2);
        counters.put(bases.v2, counters.getOrDefault(bases.v2, 0) + 2);
        counters.put(bases.v3, counters.getOrDefault(bases.v3, 0) + 2);
        lineList.add(new Drawing("polyline", bases.v1, bases.v2));
        lineList.add(new Drawing("polyline", bases.v2, bases.v3));
        lineList.add(new Drawing("polyline", bases.v3, bases.v1));
        //links.addAll(bases.split());
        splitField(field);
    }

    private void splitField(Field field) {
        GeoUtils.Triple<Point> bases = field.getBases();
        fieldList.add(new Drawing("polygon", bases.v1, bases.v2, bases.v3));
        if (field.getInners().isEmpty()) {
            return;
        }
        Point innerPoint = field.getInnerPoint();

        lineList.add(new Drawing("polyline", innerPoint, bases.v1));
        lineList.add(new Drawing("polyline", innerPoint, bases.v2));
        lineList.add(new Drawing("polyline", innerPoint, bases.v3));
        counters.put(bases.v1, counters.getOrDefault(bases.v1, 0) + 1);
        counters.put(bases.v2, counters.getOrDefault(bases.v2, 0) + 1);
        counters.put(bases.v3, counters.getOrDefault(bases.v3, 0) + 1);
        counters.put(innerPoint, counters.getOrDefault(innerPoint, 0) + 3);

        GeoUtils.Triple<Field> smallerFields = field.getSmallerFields();
        splitField(smallerFields.v1);
        splitField(smallerFields.v2);
        splitField(smallerFields.v3);
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
            this.latLngs = Arrays.asList(points).stream().map(new Function<Point, LatLngs>() {
                @Override
                public LatLngs apply(Point point) {
                    return new LatLngs(point);
                }
            }).collect(Collectors.toList());
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

    private class LatLngs {
        private double lat, lng;

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }

        public LatLngs(Point p) {
            this.lat = p.lat;
            this.lng = p.lng;
        }
    }

}
