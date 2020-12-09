package iitc.triangulation.runnables;

import iitc.triangulation.keys.KeysStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author epavlova
 * @version 24.05.2016
 */
public class StoreKeys {
  private static Logger log = LogManager.getLogger(StoreKeys.class);
  public static void main(String[] args) {
        if (args.length == 0) {
          log.error("area name not set");
            return;
        }
        Path fromPath = FileSystems.getDefault().getPath("areas", args[0], "keys.csv");
        if (!Files.exists(fromPath)) {
          log.error("keys file does not exist");
          return;
        }
        KeysStorage keys = new KeysStorage(fromPath, false);
        keys.load();
        keys.pushKeys();
        keys.store();
    }
}
