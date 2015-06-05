package iitc.triangulation;

import com.google.gson.Gson;
import iitc.triangulation.shapes.Triple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created by epavlova on 5/29/2015.
 */
public class Main {
    private static Map<String, Point> pointsById = new HashMap<>();
    private static Gson gson = new Gson();

    public static void setPoints(List<Point> points) {
        points.stream().forEach(p -> pointsById.put(p.getId(), p));
    }

    public static Triple<Point> getPoints(Triple<String> ids) {
        return Triple.of(
                pointsById.get(ids.v1),
                pointsById.get(ids.v2),
                pointsById.get(ids.v3));
    }

    private static void readPoints(Path path) {
        System.out.println(path.toAbsolutePath());
        try (Reader reader = Files.newBufferedReader(path, Charset.defaultCharset())) {
            Point[] points = gson.fromJson(reader, Point[].class);
            setPoints(Arrays.asList(points));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Path resources = FileSystems.getDefault().getPath("src", "main", "resources", "park.json");
        readPoints(resources);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("input fields");
        try {
            String fieldsString = reader.readLine();
            FieldSerializer.Drawing[] drawings = gson.fromJson(fieldsString, FieldSerializer.Drawing[].class);
            System.out.println(drawings.length + " " + Arrays.toString(drawings));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //triangulatate();

    }

    private static void triangulatate() {
        Set<Triple<Point>> set = new HashSet<>();
        Triple<Point> t1 = getPoints(Triple.of(
                "b7c2a02bf223441bb4ac4424098d4add.16", // 5
                "c19f016a8eba4ad5bd8bb4bc8e5769c8.16", //br
                "0be1cfdf37e84757b8bb348fc2c639e0.16" //mm
        ));
        set.add(t1);

        Triple<Point> t2 = getPoints(Triple.of(
                "bb2bdeda936c45a794d312e2c4e8061a.16", //a
                "c19f016a8eba4ad5bd8bb4bc8e5769c8.16",//br
                "0be1cfdf37e84757b8bb348fc2c639e0.16" //mm
        ));
        set.add(t2);
            /*Triple<Point> triple = getPoints(Triple.of(
                    "bb2bdeda936c45a794d312e2c4e8061a.16",
                    "b7c2a02bf223441bb4ac4424098d4add.16",
                    "0be1cfdf37e84757b8bb348fc2c639e0.16"));*/


        Map<Point, Set<Point>> out = new HashMap<>();
        out.computeIfAbsent(t1.v1, k -> new HashSet<>()).add(t1.v2);
        out.computeIfAbsent(t1.v2, k -> new HashSet<>()).add(t1.v3);
        out.computeIfAbsent(t1.v3, k -> new HashSet<>()).add(t1.v1);

        out.computeIfAbsent(t2.v1, k -> new HashSet<>()).add(t2.v2);

        out.computeIfAbsent(t2.v3, k -> new HashSet<>()).add(t2.v1);
                   /*baseField = new Field(baseTriangle, allPoints);
        outComingLinks.computeIfAbsent(baseTriangle.v1, k -> new HashSet<>()).add(baseTriangle.v2);
        outComingLinks.computeIfAbsent(baseTriangle.v1, k -> new HashSet<>()).add(baseTriangle.v3);
        outComingLinks.computeIfAbsent(baseTriangle.v2, k -> new HashSet<>()).add(baseTriangle.v3);
 */
        Triangulation triangulation = new Triangulation(new ArrayList<>(pointsById.values()), set, out);
        System.out.println(triangulation.run());
        FieldSerializer ser = new FieldSerializer();
        triangulation.getBaseFields().stream().forEach(f -> ser.insertField(f));
        //ser.insertField(triangulation.getBaseField());
        System.out.println(ser.serialize());
        DeployOrder deployOrder = new DeployOrder(triangulation.getBaseField());
        deployOrder.calculate();
    }

}
