package std;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class fs {

    public static Path open_file(String path) {
        try {
            var p = Paths.get(path);
            return p;
        } catch(InvalidPathException e) {
            return null;
        }
    }

    public static String read_file(Path p) {
        try {
            return Files.readString(p); // File exists
        } catch(IOException e) {
            return null;
        }
    }
    public static String write_to_file(Path p, String s) {
        try {
            Files.writeString(p, s);
            return "";
        } catch(IOException e) {
            return e.getMessage();
        }
    }
    public static void delete(Path p) {
        try {
            Files.deleteIfExists(p);
        } catch(IOException e) {
            return;
        }
    }
}
