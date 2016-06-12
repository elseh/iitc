package iitc.triangulation.runnables;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import iitc.triangulation.AbstractSerializer;
import iitc.triangulation.FieldSerializer;
import iitc.triangulation.aspect.HasValues;
import iitc.triangulation.aspect.Value;
import iitc.triangulation.aspect.ValueInjector;
import iitc.triangulation.OtherSerialization;
import iitc.triangulation.Point;
import iitc.triangulation.keys.KeysStorage;
import iitc.triangulation.other.*;
import iitc.triangulation.shapes.BaseSeed;
import iitc.triangulation.shapes.Field;
import iitc.triangulation.shapes.KeysPriorities;
import iitc.triangulation.shapes.Triple;

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
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static Path storePath;

    @Value("areaName:") private static String filename;
    @Value("other.profile:ALL_KEYS") public static KeysPriorities priorities;

    public static void main(String[] args) {
        initAll();
        System.out.println(priorities);
        Locale.setDefault(Locale.ENGLISH);
        if (filename == null || "".equals(filename)) {
            filename = FileUtils.readFromCMD.apply("Enter area name: ");
        }
        storePath = FileSystems.getDefault().getPath("areas", filename);
        Path path = FileSystems.getDefault().getPath("areas", filename + ".json");
        BaseSeed seed = gson.fromJson(FileUtils.readFromFile.apply(path), BaseSeed.class);
        KeysStorage keysStorage = new KeysStorage(storePath.resolve("keys.csv"), false);
        keysStorage.load();
        AbstractSerializer.setKeysStorage(keysStorage);
        fullTriangulate(seed, filename);
        keysStorage.store();
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
//                .limit(100)
                .map(d -> process(d, bases, full, fields))
                .filter(s -> s != null)
                .collect(Collectors.toList());

        System.out.println(serializers.size());
        Map<FieldSerializer, Double> map = new HashMap<>();
        for (FieldSerializer serializer : serializers) {
            map.put(serializer, serializer.preSerialize());
        }

        FieldSerializer goodSerializer = map.entrySet().stream()
        .min(Comparator.comparingDouble(Map.Entry::getValue))
                .get().getKey();

        writeToFile(areaName, goodSerializer.serializeMaxField(), "-maxField.json", false);
            writeToFile(areaName, goodSerializer.serializeOldText(), "-result.txt", false);

            writeToFile(areaName, goodSerializer.serialiseSVG(), ".html", false);

        for (KeysPriorities priorities : KeysPriorities.values) {
            serializeOtherByType(areaName, priorities, descriptions, fields, full);
        }
       // goodSerializer.runOtherSerializer();
    }

    private static void serializeOtherByType(String areaName, KeysPriorities priorities, Set<Description> descriptions,
                                      List<Field> fields, TriangulationFull full) {
        List<OtherSerialization> oss = descriptions.stream()
                .filter(d -> !d.getLinkAmount().entrySet()
                        .stream()
                        .filter(e -> e.getValue() > e.getKey().getMaxLinks())
                        .findFirst().isPresent())
                .map(d -> processOther(d, full, fields, priorities))
                .filter(s -> s != null)
                .collect(Collectors.toList());

        Map<OtherSerialization, Double> osMap = new HashMap<>();
        for (OtherSerialization os : oss) {
            osMap.put(os, os.process());
        }

        OtherSerialization os = osMap.entrySet().stream()
                .min(Comparator.comparingDouble(Map.Entry::getValue))
                .get().getKey();
        writeToFile(areaName, os.serializeMaxField(), "-alt-" + priorities.getName() + "-maxField.json", false);
        writeToFile(areaName, os.serializeOldText(), "-alt-" + priorities.getName() + "-result.txt", false);
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
        fields.stream().forEach(serializer::insertField);
        return serializer;
    }

    private static OtherSerialization processOther(Description description,
                                           TriangulationFull full,
                                           List<Field> fields,
                                           KeysPriorities priorities) {
        OtherSerialization serializer = new OtherSerialization(priorities);
        full.restore(description, fields);
        fields.stream().forEach(serializer::insertField);
        return serializer.baseCheck() ? serializer : null;
    }

    private static void writeToFile(String areaName, String serialize, String end, boolean show) {
        Path directory = FileSystems.getDefault().getPath("areas", areaName);
        Path result = directory.resolve(areaName + end);
        try {
            Files.createDirectories(directory);
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

    private static void initAll() {
        //ValueInjector.INSTANCE.injectStatic(Main.class);
        //ValueInjector.INSTANCE.injectStatic(OtherSerialization.class);
    }

}
