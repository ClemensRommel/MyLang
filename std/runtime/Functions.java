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

    public static <A> A panic(String message) {
        throw new RuntimeException(message);
    }

    public static ArrayList<String> args() {
        return __cli_args;
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
}
