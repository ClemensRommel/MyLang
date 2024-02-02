import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;

class Main {
    public static  sealed interface Test {
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
    public static  record B() implements Test {
        @Override public String toString() {
            StringBuilder __builder = new StringBuilder();
            __builder.append("B(");
            __builder.append(")");
            return __builder.toString();
        } 
    }
    public static void main() {
        final String first;
        first = "Hello ";
        final String second;
        second = " world";
        final __a0 __a1;
        __a1 = new __a0(first, second);
        switch(__a1) {
            case __a0(String __a2, String world) when __a2.equals("Hello ") -> {
                {
                    System.out.println(("hello " + world));
                }
            }
            case __a0(String some, String __a3) when __a3.equals(" world") -> {
                {
                    System.out.println((("some " + some) + " world"));
                }
            }
            case __a0(String hello, String world) -> {
                {
                    System.out.println((("other: " + hello) + world));
                }
            }
            
        }
    }
    
    public static void main(String[] args) {
        main();
    }
    static record __a0(String field0, String field1) {
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
    
}

