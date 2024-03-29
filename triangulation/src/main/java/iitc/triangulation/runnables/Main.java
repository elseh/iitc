package iitc.triangulation.runnables;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import iitc.triangulation.*;
import iitc.triangulation.aspect.HasValues;
import iitc.triangulation.aspect.Value;
import iitc.triangulation.keys.KeysStorage;
import iitc.triangulation.other.*;
import iitc.triangulation.shapes.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

/**
 * Created by epavlova on 5/29/2015.
 */
@HasValues
public class Main {
    private static Logger log = LogManager.getLogger(Main.class);

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static Path storePath;

    @Value("areaName:") private static String filename;
    @Value("clusterName") private static String clusterName;
    @Value("other.profile:ALL_KEYS") public static KeysPriorities priorities;

    public static void main(String[] args) {
        log.info(priorities);
        Locale.setDefault(Locale.ENGLISH);
        BaseSeed seed = getSeed();
        KeysStorage keysStorage = new KeysStorage(storePath.resolve("keys.csv"), false);
        keysStorage.load();
        AbstractSerializer.setKeysStorage(keysStorage);
        Clusters clusters = getClusters(seed);
        maxTriangulate(seed, filename);
        fullTriangulate(seed, filename, clusters);
        keysStorage.store();
    }

    private static Clusters getClusters(BaseSeed seed) {
        if (clusterName == null || "".equals(clusterName)) {
            Clusters c = new Clusters();
            c.addCluster(seed.getPoints());
            return c;
        }
        storePath = FileSystems.getDefault().getPath("clusters", clusterName);
        Path path = storePath.resolve(clusterName + ".json");
        return gson.fromJson(FileUtils.readFromFile.apply(path), Clusters.class);
    }

    private static BaseSeed getSeed() {
        if (filename == null || "".equals(filename)) {
            filename = FileUtils.readFromCMD.apply("Enter area name: ");
        }
        storePath = FileSystems.getDefault().getPath("areas", filename);
        Path path = FileSystems.getDefault().getPath("areas", filename + ".json");
        return gson.fromJson(FileUtils.readFromFile.apply(path), BaseSeed.class);
    }

    private static void maxTriangulate(BaseSeed seed, String areaName) {
        List<Point> points = seed.getPoints();
        Map<String, Point> pointById = points
                .stream()
                .collect(Collectors.toMap(Point::getId, identity()));
        Set<Triple<Point>> bases = seed.getBases()
                .stream()
                .map(t -> t.simplify(pointById::get))
                .collect(Collectors.toSet());

        bases.forEach(b -> log.info(new Field(b, new ArrayList<>(pointById.values())).getInners().size()));
        TriangulationMax full = new TriangulationMax(points);
        Set<Field> fields = bases.stream().map(b -> full.analyseSingleField(b.set())).collect(Collectors.toSet());
        //System.out.println("fields: " + fields.stream().mapToDouble(GeoUtils::fieldArea).sum());
        FieldSerializer serializer = new FieldSerializer();
        fields.forEach(serializer::insertField);
        serializer.preSerialize();
        writeToFile(areaName, serializer.serializeMaxField(), "-f-" + "-maxField.json", false);
        writeToFile(areaName, serializer.serializeOldText("txt"), "-f-"  + "-result.txt", false);
        for (KeysPriorities value : KeysPriorities.values) {
            OtherSerialization os = new OtherSerialization(value);

            fields.forEach(os::insertField);
            os.process();
            os.printStatistics();
            writeToFile(areaName, os.serializeMaxField(), "-f-" + value.getName() + "-maxField.json", false);
            writeToFile(areaName, os.serializeOldText("txt"), "-f-" + value.getName() + "-result.txt", false);
        }
    }

    private static void fullTriangulate(BaseSeed seed, String areaName, Clusters clusters) {
        List<Point> points = seed.getPoints();
        Map<String, Point> pointById = points
                .stream()
                .collect(Collectors.toMap(Point::getId, identity()));
        Set<Triple<Point>> bases = seed.getBases()
                .stream()
                .map(t -> t.simplify(pointById::get))
                .collect(Collectors.toSet());

        bases.forEach(b ->log.info(new Field(b, new ArrayList<>(pointById.values())).getInners().size()));
        TriangulationFull full = new TriangulationFull(points);

        bases.forEach(b -> full.pushBase(b.set()));
        full.startComputation();

        Set<Description> descriptions = full.calculateFields(bases.stream().map(Triple::set).collect(Collectors.toSet()));

        log.info(descriptions.size());
        List<Field> fields = bases.stream().map(b -> new Field(b, points)).collect(Collectors.toList());


        List<FieldSerializer> serializers = descriptions.stream()
                .filter(d -> d.getLinkAmount().entrySet()
                        .stream().noneMatch(e -> e.getValue() > e.getKey().getMaxLinks()))
                .sorted(Comparator.comparing(d -> d.getLinkAmount().values().stream().mapToInt(i -> i).sum()))
                .map(d -> process(d, bases, full, fields))
                .filter(Objects::nonNull)
                .limit(100)
                .collect(Collectors.toList());

        log.info(serializers.size());
        Map<FieldSerializer, AbstractSerializer.Summary> map = new HashMap<>();
        for (FieldSerializer serializer : serializers) {
            map.put(serializer, serializer.preSerialize());
        }

        FieldSerializer goodSerializer = map.entrySet().stream()
        .min(Map.Entry.comparingByValue())
                .get().getKey();

        writeToFile(areaName, goodSerializer.serializeMaxField(), "-maxField.json", false);
            writeToFile(areaName, goodSerializer.serializeOldText("txt"), "-result.txt", false);

            writeToFile(areaName, goodSerializer.serialiseSVG(), ".html", false);

        for (KeysPriorities priorities : KeysPriorities.values) {
            serializeOtherByType(areaName, priorities, descriptions, fields, full, clusters);
        }
       // goodSerializer.runOtherSerializer();
    }

    private static void serializeOtherByType(String areaName, KeysPriorities priorities, Set<Description> descriptions,
                                             List<Field> fields, TriangulationFull full, Clusters clusters) {
        List<OtherSerialization> oss = descriptions.stream()
                .filter(d -> d.getLinkAmount().entrySet()
                        .stream()
                        .noneMatch(e -> e.getValue() > e.getKey().getMaxLinks()))
                .map(d -> processOther(d, full, fields, priorities))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<OtherSerialization, Double> osMap = new HashMap<>();
        for (OtherSerialization os : oss) {
            osMap.put(os, os.process());
        }

        //System.out.println("fields: " + fields.stream().mapToDouble(GeoUtils::fieldArea).sum());
        OtherSerialization os = osMap.entrySet().stream()
                .min(Comparator.comparingDouble(Map.Entry::getValue))
                .get().getKey();
        os.printStatistics();
        writeToFile(areaName, os.serializeMaxField(), "-alt-" + priorities.getName() + "-maxField.json", false);
        writeToFile(areaName, os.serializeOldText("txt"), "-alt-" + priorities.getName() + "-result.txt", false);
        writeToFile(areaName, os.serializeOldText("html"), "-alt-" + priorities.getName() + "-result.html", false);
    }

    private static FieldSerializer process(Description description,
                                           Set<Triple<Point>> bases,
                                           TriangulationFull full,
                                           List<Field> fields) {
        FrameGenerator g = new FrameGenerator();
        Optional<Map<Point, Set<Point>>> frame = g.makeFrame(description, bases);
        if (!frame.isPresent()) return null;
        FieldSerializer serializer = new FieldSerializer();
        serializer.insertFrame(frame.get());

        full.restore(description, fields);
        fields.forEach(serializer::insertField);
        return serializer;
    }

    private static OtherSerialization processOther(Description description,
                                           TriangulationFull full,
                                           List<Field> fields,
                                           KeysPriorities priorities) {
        OtherSerialization serializer = new OtherSerialization(priorities);
        full.restore(description, fields);
        fields.forEach(serializer::insertField);
        return serializer.baseCheck() ? serializer : null;
    }

    private static void writeToFile(String areaName, String serialize, String end, boolean show) {
        Path directory = FileSystems.getDefault().getPath("areas", areaName);
        Path result = directory.resolve(areaName + end);
        try {
            Files.createDirectories(directory);
            try (BufferedWriter bw = Files.newBufferedWriter(result, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                bw.write(serialize);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (show) {
            log.info(serialize);
        }
    }
}
