package iitc.triangulation.runnables;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import iitc.triangulation.FieldSerializer;
import iitc.triangulation.old.ParallelTriangulation;
import iitc.triangulation.Point;
import iitc.triangulation.other.Description;
import iitc.triangulation.other.FrameGenerator;
import iitc.triangulation.other.TriangulationFull;
import iitc.triangulation.shapes.BaseSeed;
import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.Link;
import iitc.triangulation.shapes.Triple;

import java.io.*;
import java.nio.charset.Charset;
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
        fullTriangulate(seed, filename);
        //triangulate(seed, filename);

    }

    private static void fullTriangulate(BaseSeed seed, String areaName) {
        List<Point> points = seed.getPoints();
        Map<String, Point> pointById = points
                .stream()
                .collect(Collectors.toMap(Point::getId, identity()));
        Set<Triple<Point>> bases = seed.getBases()
                .stream()
                .map(t -> t.simplify(pointById::get))
                .collect(Collectors.toSet());

        bases.stream().forEach(b -> {
            System.out.println(new Field(b, new ArrayList<>(pointById.values())).getInners().size());
        });
        TriangulationFull full = new TriangulationFull(points);
        bases.stream().forEach(b -> full.analyseSingleField(b.set()));
        Set<Description> descriptions = full.calculateFields(bases.stream().map(Triple::set).collect(Collectors.toSet()));
        FrameGenerator g = new FrameGenerator();
        Optional<Description> first = descriptions
                .stream()
                .filter(d -> !d.getLinkAmount().entrySet()
                        .stream().filter(e -> e.getValue() > e.getKey().getMaxLinks()).findFirst().isPresent())
                .sorted(Comparator.comparing(d -> d.getLinkAmount().values().stream().mapToInt(i -> i).sum()))
                .filter(d -> g.makeFrame(d, new HashSet<>(bases)).isPresent())
                .findFirst();

        Optional<Map<Point, Set<Point>>> pointSetMap = g.makeFrame(first.get(), new HashSet<>(bases));
        if (pointSetMap.isPresent()) {
            System.out.println(pointSetMap.get());
        } else {
            System.out.println("oops no frame");
        }
        if (first.isPresent()) {
            System.out.println(first.get() + "");
            List<Field> fields = bases.stream().map(b -> new Field(b, points)).collect(Collectors.toList());
            full.restore(first.get(), fields);
            FieldSerializer serializer = new FieldSerializer();
            if (pointSetMap.isPresent()) {
                serializer.insertFrame(pointSetMap.get());
            }
            fields.stream().forEach(serializer::insertField);
            writeToFile(areaName, serializer.serialize());
        }
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

        ParallelTriangulation triangulation = new ParallelTriangulation(points, bases, links, 4);
        boolean triangulateSuccess = triangulation.triangulate();
        System.out.println(triangulateSuccess);
        if (triangulateSuccess) {
            FieldSerializer ser = new FieldSerializer();
            triangulation.getBaseFields()
                    .stream()
                    .forEach(ser::insertField);
            String serialize = ser.serialize();
            writeToFile(areaName, serialize);
        }
    }

    private static void writeToFile(String areaName, String serialize) {
        Path result = FileSystems.getDefault().getPath("areas", areaName + "-result.txt");
        try {

            try (BufferedWriter bw = Files.newBufferedWriter(result, Charset.forName("UTF-8"), StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                bw.write(serialize);
                bw.close();
            }catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(serialize);
    }

}
