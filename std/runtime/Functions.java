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
            throw new InvalidIndexException(""+d);
        }
    }

    public static int __toIndex(float d) {
        if (d % 1 == 0) {
            return (int) d;
        } else {
            throw new InvalidIndexException(""+d);
        }
    }
    public static int __toIndex(long l) {
        return (int) l;
    }
    public static int __toIndex(int l) {
        return (int) l;
    }
    public static int __toIndex(short l) {
        return (int) l;
    }
    public static int __toIndex(byte l) {
        return (int) l;
    }

    public static ArrayList<Double> __range(double start, double end) {
        ArrayList<Double> lst = new ArrayList<>();
        if(end < start) return lst;
        for (double i = start; i < end; i++) {
            lst.add(i);
        }
        return lst;
    }
    public static ArrayList<Float> __range(float start, float end) {
        ArrayList<Float> lst = new ArrayList<>();
        if(end < start) return lst;
        for (float i = start; i < end; i++) {
            lst.add(i);
        }
        return lst;
    }
    public static ArrayList<Long> __range(long start, long end) {
        ArrayList<Long> lst = new ArrayList<>();
        if(end < start) return lst;
        for (long i = start; i < end; i++) {
            lst.add(i);
        }
        return lst;
    }
    public static ArrayList<Integer> __range(int start, int end) {
        ArrayList<Integer> lst = new ArrayList<>();
        if(end < start) return lst;
        for (int i = start; i < end; i++) {
            lst.add(i);
        }
        return lst;
    }
    public static ArrayList<Short> __range(short start, short end) {
        ArrayList<Short> lst = new ArrayList<>();
        if(end < start) return lst;
        for (short i = start; i < end; i++) {
            lst.add(i);
        }
        return lst;
    }
    public static ArrayList<Byte> __range(byte start, byte end) {
        ArrayList<Byte> lst = new ArrayList<>();
        if(end < start) return lst;
        for (byte i = start; i < end; i++) {
            lst.add(i);
        }
        return lst;
    }

    public static <A> A panic(String message) {
        System.err.println("Panicked with :"+message+" at:");
        Thread.dumpStack();
        exit(1);
        throw new RuntimeException("Should have exited by now");
    }

    public static void exit(int code) {
        System.exit(code);
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
    public static boolean __equal(float a, float b) {return a == b;}
    public static boolean __equal(long a, long b) {return a == b;}
    public static boolean __equal(int a, int b) {return a == b;}
    public static boolean __equal(short a, short b) {return a == b;}
    public static boolean __equal(byte a, byte b) {return a == b;}
    public static boolean __equal(boolean a, boolean b) {return a == b;}
    public static boolean __equal(Object a, Object b) {return a == b || a.equals(b);}

    public static String __stringify(double d) {
        if(d % 1 == 0) {
            return ((int) d) + "";
        } else {
            return ""+d;
        }
    }
    public static String __stringify(float f) {
        if(f % 1 == 0) {
            return ((int) f) +"";
        } else {
            return ""+f;
        }
    }
    public static String __stringify(long v) {
        return "" + v;
    }
    public static String __stringify(int v) {
        return "" + v;
    }
    public static String __stringify(short v) {
        return "" + v;
    }
    public static String __stringify(byte v) {
        return "" + v;
    }
    public static String __stringify(boolean b) {return ""+b;}
    public static String __stringify(Double d) {
        if(d % 1 == 0) {
            return ((int) (double) d) + "";
        } else {
            return ""+d;
        }
    }
    public static String __stringify(Float f) {
        if(f % 1 == 0) {
            return ((int) (float) f) + "";
        } else {
            return "" + f;
        }
    }
    public static String __stringify(Object o) {
        if(o instanceof Double d) {return __stringify(d);}
        if(o instanceof Float f) {return __stringify(f);}
        return o.toString();
    }
}
