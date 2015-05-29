package iitc.triangulation;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Created by epavlova on 5/29/2015.
 */
public class Main {
    public static void main(String[] args) {
        Gson gson = new Gson();
        Path resources = FileSystems.getDefault().getPath("src", "main", "resources", "portals.json");
        System.out.println(resources.toAbsolutePath());
        try (Reader reader = Files.newBufferedReader(resources, Charset.defaultCharset())){
            Point[] points = gson.fromJson(reader, Point[].class);
            System.out.println(Arrays.deepToString(points));
            filterPoints(points, new Point[]{points[2], points[8], points[11]});
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void filterPoints(Point[] all, Point[] triangle) {
        for (Point p : all) {
            if (GeoUtils.isPointInsideField(p, triangle[0], triangle[1], triangle[2])) {
                System.out.println("inside: " + p);
            } else {
                System.out.println("outside: " + p);
            }
        }
    }
}
