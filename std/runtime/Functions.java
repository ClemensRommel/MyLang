package std.runtime;

import java.util.ArrayList;
import java.util.Arrays;

import java.nio.file.*;

public class Functions {
    // Ignore a value. Used for expression statements and main()
    public static <A> void __ignore(A a) {

    }

    public static int __toIndex(double d) {
        if (d % 1 == 0) {
            return (int) d;
        } else {
            throw new InvalidIndexException(d);
        }
    }
    public static ArrayList<Double> __range(double start, double end) {
        ArrayList<Double> lst = new ArrayList<>();
        if(end < start) return lst;
        for (double i = start; i < end; i++) {
            lst.add(i);
        }
        return lst;
    }

    public static double len(String str) {
        return (double) str.length();
    }

    public static <A> A panic(String message) {
        throw new RuntimeException(message);
    }
    public static Void print(String message) {
        System.out.println(message);
        return null;
    }

    public static ArrayList<String> args() {
        return __cli_args;
    }

    public static String join(String sep, ArrayList<String> lst) {
        return String.join(sep, lst);
    }

    public static ArrayList<String> split(String to_be_splitted, String splitter) {
        var result = to_be_splitted.split(splitter);
        return new ArrayList<>(Arrays.asList(result));
    }

    public static double number(String from) {
        return Double.parseDouble(from);
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

    public static Void writeToFile(String path, String content) {
        try {
            Files.writeString(Paths.get(path), content);
        } catch (Exception e) {
            panic("Could not write to file: "+path);
        }
        return null;
    }

    public static boolean isNull(Object o) {
        return o == null;
    }

    public static String replace(String old, String searched, String replacement) {
        return old.replace(searched, replacement);
    }

    private static ArrayList<String> __cli_args;

    public static void __init_runtime(String[] args) {
        var args_list = Arrays.asList(args);
        if(args.length > 1) {
            args_list = args_list.subList(1, args.length);
        }
        __cli_args = new ArrayList<>(args_list);
    }

    public static boolean __equal(double a, double b) {return a == b;}
    public static boolean __equal(boolean a, boolean b) {return a == b;}
    public static boolean __equal(Object a, Object b) {return a == b || a.equals(b);}

    public static String __stringify(double d) {
        if(d % 1 == 0) {
            return ((int) d) + "";
        } else {
            return ""+d;
        }
    }
    public static String __stringify(boolean b) {return ""+b;}
    public static String __stringify(Double d) {
        if(d % 1 == 0) {
            return ((int) (double) d) + "";
        } else {
            return ""+d;
        }
    }
    public static String __stringify(Object o) {
        if(o instanceof Double d) {return __stringify(d);}
        return o.toString();
    }

    public static Void move_line_up() {
        System.out.print("\033[1A\033[2K");
        return null;
    }
}
