package iitc.triangulation;

import com.google.gson.Gson;
import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.LatLngs;
import iitc.triangulation.shapes.Triple;

import java.util.*;
import java.util.stream.Collectors;

import static iitc.triangulation.DeployOrder.*;

/**
 * Created by Sigrlinn on 31.05.2015.
 */
public class FieldSerializer {
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
        String pattern = "<svg height=\"70%%\" width=\"70%%\" id = \"test\">\n" +
                "%s \n" +
                "%s\n" +
                "</svg>\n" +
                "<style>\n" +
                "line {stroke-width:3; stroke:gray;}\n" +
                ".path {stroke-width:3; stroke:green;}\n" +
                ".apath .path{display:initial;}\n" +
                "%s\n" +
                "</style>\n" +
                "<button onClick=\"add();\">click</button>\n" +
                "<button onClick=\"hidePath();\">toggle path</button>\n" +
                "<button onClick=\"cl();\">clear</button>" +
                "<script>var i = 0;\n" +
                "\n" +
                "function add() {\n" +
                "\tdocument.body.className += \" active\" + i;\n" +
                "\ti++;\n" +
                "};\n" +
                "\n" +
                "var v=true;\n" +
                "function hidePath() {\n" +
                "\tif (v) {\n" +
                "\t\tdocument.body.className = document.body.className.replace(\" p \", \" apath \");\n" +
                "\t} else {\n" +
                "\t\tdocument.body.className = document.body.className.replace(\" apath \", \" p \");\n" +
                "\t}\n" +
                "\tv = !v;\n" +
                "};\n" +
                "\n" +
                "function cl() {\n" +
                "\ti = 0;\n" +
                "\tv = true;\n" +
                "\tdocument.body.className = \" p \";\n" +
                "};\n</script>";

        SVGSerializer serializer = new SVGSerializer(linksOrder, pointsOrder);

        return String.format(pattern,
                serializer.makePoints(),
                serializer.makeLines(),
                serializer.makeStyles()
        );
    }

    public String serialize() {
        Gson gson = new Gson();

        String result = linksMap.entrySet()
                .stream()
                .map(e -> e.getKey().getTitle() + " : " + e.getValue().size())
                .collect(Collectors.joining("\n"));

        String noLinks = linksMap.entrySet()
                .stream()
                .filter(e -> e.getValue().size() == 3)
                .map(e -> e.getKey().getTitle())
                .sorted()
                .collect(Collectors.joining("\n"));

        String withLinks = linksMap.entrySet()
                .stream()
                .filter(e -> e.getValue().size() > 3)
                .map(e -> e.getKey().getTitle())
                .sorted()
                .collect(Collectors.joining("\n"));

        DeployOrder dOrder = new DeployOrder(linksOrder);

       pointsOrder = dOrder.extractPointOrder();


        return new StringBuilder()
                .append("\n linksAmount : \n").append(result).append("\n")
                .append("\n inners : \n").append(noLinks).append("\n")
                .append("\n bases : \n").append(withLinks).append("\n")
                .append("\n fields: \n").append(gson.toJson(fieldList)).append("\n")
                .append("\n links: \n").append(gson.toJson(lineList)).append("\n")
                .append("\n links order: \n").append(
                        pointsOrder
                                .stream()
                                .filter(p -> linksOrder.get(p).size() > 0)
                                .map(p -> p.getTitle() + " : " + linksOrder.get(p).size() + "\n\t"
                                        + linksOrder.get(p)
                                        .stream()
                                        .map(Point::getTitle)
                                        .collect(Collectors.joining("\n\t")))
                                .collect(Collectors.joining("\n"))
                ).append("\n")
                .append("\n points order: \n").append(gson.toJson(new Drawing[]{new Drawing("polyline", pointsOrder.toArray(new Point[pointsOrder.size()]))})).append("\n")
                .append("length: ").append(length(pointsOrder)).append("\n")
                .toString();
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
