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
        final Test test;
        test = new B();
        switch(test) {
            case A(double n) -> {
                {
                    System.out.println("A");
                }
            }
            case Test __a0 -> {
                {
                    System.out.println("B");
                }
            }
            
        }
    }
    
    public static void main(String[] args) {
        main();
    }
    
}

