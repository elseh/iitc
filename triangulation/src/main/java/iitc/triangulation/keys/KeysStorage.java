package iitc.triangulation.keys;

import iitc.triangulation.Point;
import iitc.triangulation.shapes.LatLngs;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.file.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author epavlova
 * @version 22.05.2016
 */
public class KeysStorage {
    public static final Path STORAGE_PATH = FileSystems.getDefault().getPath("keys", "keys.csv");
    public static KeysStorage INSTANCE = new KeysStorage(STORAGE_PATH, true);
    private Map<String, Key> storage = new HashMap<>();

    private Path path;
    private boolean isMain;
    public KeysStorage(Path path, boolean isMain) {
        this.path = path;
        this.isMain = isMain;
    }

    public void load() {
        try {
            if (Files.exists(path)) {
                List<String> keysList = Files.readAllLines(path);
                keysList.stream().forEach(
                        s -> {
                            Key key = new Key().fromString(s);
                            if (key != null) {
                                storage.put(key.id, key);
                            }
                        }
                );

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!isMain) {
            INSTANCE.load();
        }
        System.out.println("load: " + isMain + " " + storage.size());
    }

    public int keysFor(Point point) {
        return storage.computeIfAbsent(id(point), p ->
                new Key().fromPoint(point, isMain ? 0 : INSTANCE.keysFor(point))
        ).amount;
    }

    public void store() {
        try {
            Files.createDirectories(path.getParent());
            Files.write(
                    path,
                    storage.values().stream()
                            .sorted(Comparator.comparing(k -> k.name))
                            .map(Key::toString)
                            .collect(Collectors.toList()),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE
            );
            //System.out.println(storage);
            if (!isMain) {
                INSTANCE.store();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String id(Point p) {
        return String.format("%.6f:%.6f", p.getLatlng().getLat(), p.getLatlng().getLng()).intern();
    }
    public void pushKeys() {
        INSTANCE.storage.putAll(storage);
    }

    public static class Key {
        private String id;
        private String name;
        private int amount;
        private LatLngs ll;

        public Key fromPoint(Point p, int amount) {
            name = p.getTitle();
            this.amount = amount;
            this.ll = p.getLatlng();
            makeId();
            return this;
        }

        public Key fromString(String string) {
            String[] split = string.split(",");
            if (split.length != 4) {
                return null;
            }
            ll = new LatLngs(Double.parseDouble(split[0].trim()),Double.parseDouble(split[1].trim()));
            makeId();
            name = split[2].trim();
            amount = Integer.parseInt(split[3].trim());
            return this;
        }

        public void makeId() {
            id = String.format("%.6f:%.6f", ll.getLat(), ll.getLng()).intern();
        }

        public String toString() {
            return String.format("%.6f, %.6f, %s, %d", ll.getLat(), ll.getLng(), name, amount);
        }
    }
}
