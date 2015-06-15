package iitc.triangulation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import iitc.triangulation.shapes.BaseSeed;
import iitc.triangulation.shapes.Link;
import iitc.triangulation.shapes.Triple;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

/**
 * Created by epavlova on 5/29/2015.
 */
public class Main {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        String filename = FileUtils.readFromCMD.apply("Enter area name: ");
        Path path = FileSystems.getDefault().getPath("areas", filename + ".json");
        BaseSeed seed = gson.fromJson(FileUtils.readFromFile.apply(path), BaseSeed.class);
        triangulate(seed, filename);

    }

    private static void triangulate(BaseSeed seed, String areaName) {
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
        ParallelTriangulation triangulation = new ParallelTriangulation(points, bases, links, 4);
        boolean triangulateSuccess = triangulation.triangulate();
        System.out.println(triangulateSuccess);
        if (triangulateSuccess) {
            FieldSerializer ser = new FieldSerializer();
            triangulation.getBaseFields()
                    .stream()
                    .forEach(ser::insertField);
            String serialize = ser.serialize();
            Path result = FileSystems.getDefault().getPath("areas", areaName + "-result.txt");
            try {

                try (BufferedWriter bw = Files.newBufferedWriter(result, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                    bw.write(serialize);
                    bw.close();
                }catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }


                /*Files.createFile(result);
                Files.write(result, serialize.getBytes());*/
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println(serialize);
        }
    }

}
