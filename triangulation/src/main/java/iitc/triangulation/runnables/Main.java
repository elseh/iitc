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
        Locale.setDefault(Locale.ENGLISH);
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

        bases.stream().forEach(b ->
            System.out.println(new Field(b, new ArrayList<>(pointById.values())).getInners().size())
        );
        TriangulationFull full = new TriangulationFull(points);
        bases.stream().forEach(b -> full.analyseSingleField(b.set()));
        Set<Description> descriptions = full.calculateFields(bases.stream().map(Triple::set).collect(Collectors.toSet()));

/*        FrameGenerator g = new FrameGenerator();
        Description testD = Description.makeEmptyBase(bases.stream().findAny().get().set());
        testD.test();
        for (int i = 0; i < 10; i++) {
            System.out.println(testD.testAdd(g.makeFrame(testD, new HashSet<>(bases)).get()));
        }*/

        System.out.println(descriptions.size());
        List<Field> fields = bases.stream().map(b -> new Field(b, points)).collect(Collectors.toList());


        List<FieldSerializer> serializers = descriptions.stream()
                .filter(d -> !d.getLinkAmount().entrySet()
                        .stream().filter(e -> e.getValue() > e.getKey().getMaxLinks()).findFirst().isPresent())
                .sorted(Comparator.comparing(d -> d.getLinkAmount().values().stream().mapToInt(i -> i).sum()))
                .limit(100)
                .map(d -> process(d, bases, full, fields))
                .filter(s -> s != null)
                .collect(Collectors.toList());
        System.out.println(serializers.size());
        Map<FieldSerializer, Double> map = new HashMap<>();
        for (FieldSerializer serializer : serializers) {
            map.put(serializer, serializer.preSerialize());
        }
        FieldSerializer goodSerializer = map.entrySet().stream()
        .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                .findFirst().get().getKey();

            writeToFile(areaName, goodSerializer.serialize(), "-result.txt", true);
            writeToFile(areaName, goodSerializer.serialiseSVG(), ".html", false);

    }

    private static FieldSerializer process(Description description,
                                           Set<Triple<Point>> bases,
                                           TriangulationFull full,
                                           List<Field> fields) {
        FrameGenerator g = new FrameGenerator();
        long current = System.currentTimeMillis();
        System.out.println("frame for : " + description);
        Optional<Map<Point, Set<Point>>> frame = g.makeFrame(description, bases);
        System.out.println(frame.isPresent() + "fraME: " + (System.currentTimeMillis() - current));

        if (!frame.isPresent()) return null;

        FieldSerializer serializer = new FieldSerializer();
        serializer.insertFrame(frame.get());

        current = System.currentTimeMillis();

        full.restore(description, fields);
        System.out.println("restore: " + (System.currentTimeMillis() - current));
        current = System.currentTimeMillis();
        fields.stream().forEach(serializer::insertField);
        System.out.println("setup: " + (System.currentTimeMillis() - current));
        return serializer;
    }

    private static void writeToFile(String areaName, String serialize, String end, boolean show) {
        Path result = FileSystems.getDefault().getPath("areas", areaName + end);
        try {

            try (BufferedWriter bw = Files.newBufferedWriter(result, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                bw.write(serialize);
                bw.close();
            }catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (show) {
            System.out.println(serialize);
        }
    }

}
