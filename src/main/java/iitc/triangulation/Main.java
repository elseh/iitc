package iitc.triangulation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import iitc.triangulation.shapes.BaseSeed;
import iitc.triangulation.shapes.Link;
import iitc.triangulation.shapes.Triple;

import java.nio.file.FileSystems;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

/**
 * Created by epavlova on 5/29/2015.
 */
public class Main {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        BaseSeed seed = gson.fromJson(FileUtils.readFromFile.apply(FileSystems.getDefault().getPath("src", "main", "resources", "bgsl.json")), BaseSeed.class);
        triangulate(seed);

    }

    private static void triangulate(BaseSeed seed) {
        List<Point> points = seed.getPoints();
        Map<String, Point> pointById = points
                .stream()
                .collect(Collectors.toMap(Point::getId, identity()));
        Set<Triple<Point>> bases = seed.getBases()
                .stream()
                .map(t -> t.simplify(pointById::get))
                .collect(Collectors.toSet());
        Map<Point, Set<Point>> links = seed.getLinks()
                .stream()
                .map(Link::getLink)
                .map(v -> v.simplify(pointById::get))
                .collect(Collectors.groupingBy(p -> p.v1, Collectors.mapping(p -> p.v2, Collectors.toSet())));


        /*Triangulation triangulation = new Triangulation(points, bases, links);
        System.out.println(triangulation.run());*/
        ParallelTriangulation triangulation = new ParallelTriangulation(points, bases, links, 8);
        System.out.println(triangulation.triangulate());
        FieldSerializer ser = new FieldSerializer();
        triangulation.getBaseFields()
                .stream()
                .forEach(ser::insertField);
        System.out.println(ser.serialize());
    }

}
