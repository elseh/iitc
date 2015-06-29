package iitc.triangulation.runnables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by epavlova on 6/5/2015.
 */
public class FileUtils {
    static Function<Path, String> readFromFile = path -> {
        System.out.println(path.toAbsolutePath());
        try {
            return Files.readAllLines(path).stream().collect(Collectors.joining());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    };
    static Function<String, String> readFromCMD = message -> {
        System.out.println(message);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            return  reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    };
}
