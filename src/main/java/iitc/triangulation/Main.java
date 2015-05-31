package iitc.triangulation;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by epavlova on 5/29/2015.
 */
public class Main {
    private static Map<String, Point> pointsById = new HashMap<>();

    public static void setPoints(List<Point> points) {
        points.stream().forEach(new Consumer<Point>() {
            @Override
            public void accept(Point point) {
                pointsById.put(point.getId(), point);
            }
        });
    }

    public static GeoUtils.Triple<Point> getPoints(GeoUtils.Triple<String> ids) {
        return GeoUtils.Triple.of(
                pointsById.get(ids.v1),
                pointsById.get(ids.v2),
                pointsById.get(ids.v3));
    }
    public static void main(String[] args) {
        Gson gson = new Gson();
        Path resources = FileSystems.getDefault().getPath("src", "main", "resources", "park.json");
        System.out.println(resources.toAbsolutePath());
        try (Reader reader = Files.newBufferedReader(resources, Charset.defaultCharset())){
            Point[] points = gson.fromJson(reader, Point[].class);
            setPoints(Arrays.asList(points));
            GeoUtils.Triple<Point> triple = getPoints(GeoUtils.Triple.of(
                    "bb2bdeda936c45a794d312e2c4e8061a.16",
                    "b7c2a02bf223441bb4ac4424098d4add.16",
                    "0be1cfdf37e84757b8bb348fc2c639e0.16"));
            Triangulation triangulation = new Triangulation(Arrays.asList(points), triple);
            System.out.println(triangulation.run());
            FieldSerializer ser = new FieldSerializer();
            ser.insertField(triangulation.getBaseField());
            System.out.println(ser.serialize());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
