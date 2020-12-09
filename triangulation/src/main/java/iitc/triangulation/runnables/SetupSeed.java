package iitc.triangulation.runnables;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import iitc.triangulation.Drawing;
import iitc.triangulation.Point;
import iitc.triangulation.keys.KeysStorage;
import iitc.triangulation.shapes.BaseSeed;
import iitc.triangulation.RawData;
import iitc.triangulation.shapes.LatLngs;
import iitc.triangulation.shapes.Link;
import iitc.triangulation.shapes.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

/**
 * Created by epavlova on 6/5/2015.
 */
public class SetupSeed {
    private static Logger log = LogManager.getLogger(SetupSeed.class);

    private static Map<String, Point> pointsById = new HashMap<>();
    private static Map<LatLngs, Point> pointsByLat = new HashMap<>();
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void setPoints(List<Point> points) {
        pointsById = points.stream().collect(Collectors.toMap(Point::getId, identity(), (a,b) -> a));
        pointsByLat = points.stream().collect(Collectors.toMap(Point::getLatlng, identity(),  (a,b) -> a));
    }

    private static void readPoints(Path path) {
        Point[] points = gson.fromJson(FileUtils.readFromFile.apply(path), Point[].class);
        setPoints(Arrays.asList(points));
    }

    public static void main(String[] args) {
        RawData rawData = gson.fromJson(FileUtils.readFromCMD.apply("Enter points: "), RawData.class);
        setPoints(Arrays.asList(rawData.getPoints()));
        BaseSeed seed = parseDrawing(rawData.getDrawings());
        String fileName = rawData.getName();

        writeToFile(fileName, gson.toJson(seed));
        /*try {
            Path areas = FileSystems.getDefault().getPath("areas", fileName + ".json");
            Files.createFile(areas);
            Files.write(areas, gson.toJson(seed).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        //System.out.println(gson.toJson(seed));
    }

    private static BaseSeed parseDrawing(Drawing[] drawings) {
        List<Link> links = new ArrayList<>();
        List<Triple<String>> triples = new ArrayList<>();
        for (Drawing d : drawings) {
            log.info(d);
            log.info(new Gson().toJson(d));
            List<Point> points = d.getLatLngs().stream().map(pointsByLat::get).collect(Collectors.toList());
            if (points.size() != 4 || points.get(0) != points.get(3)) {
                log.error("Point list size is not 4 {} or last point != first point {}", points.size(), points);
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

    private static Drawing[] readDrawings(Path path) {
        return gson.fromJson(FileUtils.readFromFile.apply(path), Drawing[].class);
    }

    private static void writeToFile(String areaName, String serialize) {
        Path result = FileSystems.getDefault().getPath("areas", areaName + ".json");
        try {

            try (BufferedWriter bw = Files.newBufferedWriter(result, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                bw.write(serialize);
            }catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}