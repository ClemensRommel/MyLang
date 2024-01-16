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
        final Test testval;
        testval = new A(3.0);
        System.out.println(testval);
    }
    
    public static void main(String[] args) {
        main();
    }
    
}

