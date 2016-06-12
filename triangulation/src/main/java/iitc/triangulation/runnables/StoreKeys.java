package iitc.triangulation.runnables;

import iitc.triangulation.keys.KeysStorage;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author epavlova
 * @version 24.05.2016
 */
public class StoreKeys {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("area name not set");
            return;
        }
        Path fromPath = FileSystems.getDefault().getPath("areas", args[0], "keys.csv");
        if (!Files.exists(fromPath)) {
            System.out.println("keys file does not exist");

return;
        }
        KeysStorage keys = new KeysStorage(fromPath, false);
        keys.load();
        keys.pushKeys();
        keys.store();
    }
}
