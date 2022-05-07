package iitc.triangulation.runnables;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import iitc.triangulation.Drawing;
import iitc.triangulation.GeoUtils;
import iitc.triangulation.Point;
import iitc.triangulation.shapes.*;
import iitc.triangulation.RawData;
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
public class SetupClusters {
    private static Logger log = LogManager.getLogger(SetupClusters.class);

    private static List<Point> points = new ArrayList<>();
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        RawData rawData = gson.fromJson(FileUtils.readFromCMD.apply("Enter points: "), RawData.class);
        points = Arrays.asList(rawData.getPoints());
        Clusters clusters = parseDrawing(rawData.getDrawings());
        String fileName = rawData.getName();
        writeToFile(fileName, gson.toJson(clusters));
    }

    private static Clusters parseDrawing(Drawing[] drawings) {
        Clusters result = new Clusters();
        for (Drawing d : drawings) {
            log.info(d);
            log.info(new Gson().toJson(d));
            result.addCluster(
                points.stream()
                .filter(p -> GeoUtils.isPointInsideDrawing(p, d.getLatLngs()))
                .collect(Collectors.toList())
            );

        }
        return result;
    }

    private static void writeToFile(String areaName, String serialize) {
        Path directory = FileSystems.getDefault().getPath("clusters");
        Path result = directory.resolve(areaName + ".json");

        try {
            Files.createDirectories(directory);
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