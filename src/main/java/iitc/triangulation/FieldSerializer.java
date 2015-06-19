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
    //private Map<Point, Integer> counters = new HashMap<>();
    private Map<Point, List<Point>> linksOrder = new HashMap<>();

    private Map<Point, Set<Point>> linksMap = new HashMap<>();

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
        splitField(field, 0);
    }

    private void splitField(Field field, int deep) {
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

            field.getSmallerFields().stream().forEach(v -> splitField(v, deep + 1));
        }
    }

    public void writeDown(int deep, Point p) {
        String name = p == null ? "()" : p.getTitle();
        System.out.printf("%3d %" + (deep*3+1) + "s %s\n", deep, "", name);
    }

    public String serialize() {
        Gson gson = new Gson();

        String result = linksMap.entrySet()
                .stream()
                .map(e -> e.getKey().getTitle() + " : " + e.getValue().size())
                .collect(Collectors.joining("\n"));

        List<Point> pointsOrder = extractOrder();
        return new StringBuilder()
                .append("linksAmount : \n").append(result).append("\n")
                .append("fields: \n").append(gson.toJson(fieldList)).append("\n")
                .append("links: \n").append(gson.toJson(lineList)).append("\n")
                .append("links order: \n").append(
                        pointsOrder
                                .stream()
                                .filter(p -> linksOrder.get(p).size() > 0)
                                .map(p -> p.getTitle() + " : \n    "
                                        + linksOrder.get(p)
                                        .stream()
                                        .map(Point::getTitle)
                                        .collect(Collectors.joining("\n     ")))
                                .collect(Collectors.joining("\n"))
                ).append("\n")
                .append("points order: \n").append(gson.toJson(new Drawing[]{new Drawing("polyline", pointsOrder.toArray(new Point[0]))})).append("\n")
                .toString();
    }

    private List<Point> extractOrder() {
        List<Point> result = new ArrayList<>();
        Map<Point, List<Point>> copy = new HashMap<>();
        linksOrder.forEach((k, v) -> copy.put(k, new ArrayList<>(v)));
        while (result.size() < linksOrder.size()) {
            List<Point> toRemove = copy.entrySet()
                    .stream()
                    .filter(e -> e.getValue().size() == 0)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            result.addAll(toRemove);
            toRemove.forEach(copy::remove);
            toRemove.forEach(p -> copy.values().stream().forEach(v -> v.remove(p)));
        }
        return result;
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
