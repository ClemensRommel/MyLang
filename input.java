import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;

class Main {
    public static double f(double x)  {
        return x * 2.0;
    }
    public static class Test {
        public final double test;
        {
            test = 3.0;
        }
        public Test getTest()  {
            return this;
        }
    }
    public static void main() {
        System.out.println(1.0);
        System.out.println("test");
        System.out.println(true);
        System.out.println("null");
        double x;
        x = 1.0;
        System.out.println(x);
        System.out.println((x + 1.0));
        System.out.println((-x));
        var __a0 = f(x);
        System.out.println(__a0);
        final __a2 __a1 = (double y) -> {
            return 3.0 * y;
        };
        var __a3 = __a1.call(x);
        System.out.println(__a3);
        final double __a4;
        if(true) {
            __a4 = x;
        } else {
            __a4 = 3.0;
        }
        System.out.println(__a4);
        ArrayList<Double> __a5 = new ArrayList<>(3);
        __a5.add(1.0);
        __a5.add(2.0);
        __a5.add(3.0);
        System.out.println(__a5);
        System.out.println(__range(1.0, 3.0));
        ArrayList<Double> __a6 = new ArrayList<>(3);
        __a6.add(1.0);
        __a6.add(2.0);
        __a6.add(3.0);
        System.out.println(__a6.get(__toIndex(1.0)));
        var __a7 = new Test();
        System.out.println(__a7.test);
        final __a8 __a9;
        __a9 = new __a8(1.0, 2.0);
        System.out.println(__a9);
        final String __a10;
        {
            System.out.println("hello ");
            __a10 = "world";
        }
        System.out.println(__a10);
        final ArrayList<Double> __a11;
        __a11 = new ArrayList<>();
        while(true) {
            if(!(x < 5.0)) { break; }
            final double __a12;
            {
                x = x + 1.0;
                __a12 = x;
            }
            __a11.add(__a12);
            
        }
        System.out.println(__a11);
        final ArrayList<Double> __a14;
        __a14 = new ArrayList<>();
        final double __a15;__a15 = 1.0;
        final double __a16;__a16 = 4.0;
        if(__a15 < __a16) 
        for(double __a13 = __a15; __a13 < __a16; __a13++) {
            final double y;
            y = __a13;
            final double __a17;
            {
                __a17 = y + 1.0;
            }
            __a14.add(__a17);
            
        }
        System.out.println(__a14);
        var __a18 = new Test();
        var __a19 = __a18.getTest();
        System.out.println(__a19);
    }
    
    public static void main(String[] args) {
        main();
    }
    static record __a8(double field0, double field1) {
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("(");
            builder.append(field0);
            builder.append(", ");
            builder.append(field1);
            builder.append(")");
            return builder.toString();
        }
    }
    @FunctionalInterface static interface __a2 {
        double call(double __a20);
    }
    
}

