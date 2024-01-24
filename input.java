import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;

class Main {
    public static  sealed interface Test {
        public default double get_one()  {
            return 1.0;
        }
    }
    public static  record A(double __field0) implements Test {
        @Override public String toString() {
            StringBuilder __builder = new StringBuilder();
            __builder.append("A(");
            __builder.append(__field0());
            __builder.append(")");
            return __builder.toString();
        } 
    }
    public static  record B(String __field0, double __field1) implements Test {
        @Override public String toString() {
            StringBuilder __builder = new StringBuilder();
            __builder.append("B(");
            __builder.append(__field0());
            __builder.append(", ");__builder.append(__field1());
            __builder.append(")");
            return __builder.toString();
        } 
    }
    public static  record C(ArrayList<Test> __field0) implements Test {
        @Override public String toString() {
            StringBuilder __builder = new StringBuilder();
            __builder.append("C(");
            __builder.append(__field0());
            __builder.append(")");
            return __builder.toString();
        } 
    }
    public static void main() {
        final double x;
        x = -1.5;
        final String __a0;
        switch((Double) x) {
            case Double __a1 when __a1 == 3 -> {
                __a0 = "3";
            }
            case Double __a2 when __a2 == 4 -> {
                __a0 = "4";
            }
            case Double y -> {
                __a0 = "other";
            }
            
        }
        System.out.println(__a0);
    }
    
    public static void main(String[] args) {
        main();
    }
    
}

