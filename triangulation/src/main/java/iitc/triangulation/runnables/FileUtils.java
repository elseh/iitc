package iitc.triangulation.runnables;

import iitc.triangulation.keys.KeysStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by epavlova on 6/5/2015.
 */
public class FileUtils {
    private static Logger log = LogManager.getLogger(FileUtils.class);

    public static final InputStreamReader IN = new InputStreamReader(System.in, StandardCharsets.UTF_8);
    public static BufferedReader CMD = new BufferedReader(IN);
    static Function<Path, String> readFromFile = path -> {
        log.info(path.toAbsolutePath());
        try {
            return String.join("", Files.readAllLines(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    };
    static Function<String, String> readFromCMD = message -> {
        log.info(message);
        try {
            return CMD.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    };


}
