package iitc.triangulation.runnables;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import iitc.triangulation.Point;
import iitc.triangulation.shapes.BaseSeed;
import iitc.triangulation.FieldSerializer;
import iitc.triangulation.shapes.LatLngs;
import iitc.triangulation.shapes.Link;
import iitc.triangulation.shapes.Triple;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;

/**
 * Created by epavlova on 6/5/2015.
 */
public class SetupSeed {
    private static Map<String, Point> pointsById = new HashMap<>();
    private static Map<LatLngs, Point> pointsByLat = new HashMap<>();
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void setPoints(Point[] points) {
        pointsById = Stream.of(points)
                .collect(Collectors.toMap(Point::getId, identity(), (a, b) -> a));
        pointsByLat = Stream.of(points)
                .collect(Collectors.toMap(Point::getLatlng, identity(), (a, b) -> a));
    }

    public static void main(String[] args) {
        String fileName = "";
        BaseSeed seed = null;
        try(JsonReader reader = new JsonReader(FileUtils.IN)) {
            reader.beginArray();
                setPoints(gson.fromJson(reader, Point[].class));
                seed = parseDrawing(gson.fromJson(reader, FieldSerializer.Drawing[].class));
                fileName = gson.fromJson(reader, String.class);
            reader.endArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Path areas = FileSystems.getDefault().getPath("areas", fileName + ".json");
        try (BufferedWriter bw = Files.newBufferedWriter(areas, Charset.forName("UTF-8"), StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            bw.write(gson.toJson(seed));
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BaseSeed parseDrawing(FieldSerializer.Drawing[] drawings) {
        List<Link> links = new ArrayList<>();
        List<Triple<String>> triples = new ArrayList<>();
        for (FieldSerializer.Drawing d : drawings) {

            List<Point> points = d.getLatLngs().stream().map(pointsByLat::get).collect(Collectors.toList());
            if (points.size() != 4 || points.get(0) != points.get(3)) {
                System.out.println("Panic");
            }
            for (int i = 1; i < points.size(); i++) {
                links.add(new Link(points.get(i - 1), points.get(i)));
            }
            triples.add(Triple.of(points.get(0).getId(),
                            points.get(1).getId(),
                            points.get(2).getId())
            );
        }
        return new BaseSeed(triples, new ArrayList<>(pointsById.values()), links);
    }
}


