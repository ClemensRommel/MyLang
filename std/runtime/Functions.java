package std.runtime;

import java.util.ArrayList;

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

    public static Void panic(String message) {
        throw new RuntimeException(message);
    }
}
