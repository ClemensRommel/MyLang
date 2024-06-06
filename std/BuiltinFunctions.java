package std;

import static std.runtime.Functions.panic;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class BuiltinFunctions {
    public static Void move_line_up() {
        System.out.print("\033[1A\033[2K");
        return null;
    }
    public static String join(String sep, ArrayList<String> lst) {
        return String.join(sep, lst);
    }
    public static ArrayList<String> split(String to_be_splitted, String splitter) {
        var result = to_be_splitted.split(splitter);
        return new ArrayList<>(Arrays.asList(result));
    }
    public static double len(String str) {
        return (double) str.length();
    }
    public static int int_len(String str) {
        return str.length();
    }
    public static String strip(String str) {
        return str.strip();
    }
    public static boolean matches(String str, String regex) {
        return str.matches(regex);
    }
    public static String openFile(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (Exception e) {
            return null;
        }
    }
    public static boolean isNull(Object o) {
        return o == null;
    }
    public static Void writeToFile(String path, String content) {
        try {
            Files.writeString(Paths.get(path), content);
        } catch (Exception e) {
            panic("Could not write to file: "+path);
        }
        return null;
    }
    public static double number(String from) {
        return Double.parseDouble(from);
    }
    public static String replace(String old, String searched, String replacement) {
        return old.replace(searched, replacement);
    }
    public static int int_from_number(double d) {
        return (int) d;
    }
}
