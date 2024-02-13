import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;

import test.Test;

public class input {
    public static  sealed interface Test {
    }
    public static  record A(String __field0) implements Test {
        @Override public String toString() {
            StringBuilder __builder = new StringBuilder();
            __builder.append("A(");
            __builder.append(__field0());
            __builder.append(")");
            return __builder.toString();
        } 
    }
    public static  record B(double __field0) implements Test {
        @Override public String toString() {
            StringBuilder __builder = new StringBuilder();
            __builder.append("B(");
            __builder.append(__field0());
            __builder.append(")");
            return __builder.toString();
        } 
    }
    public static void main() {
        final ArrayList<Test> tests;
        tests = new ArrayList<>(4);
        var __var0 = new A("a");
        tests.add(__var0);
        var __var1 = new A("b");
        tests.add(__var1);
        var __var2 = new A("c");
        tests.add(__var2);
        var __var3 = new B(6.0);
        tests.add(__var3);
        double index;
        index = -1.0;
        __var4: while(true) {
            {
                final double __var5;
                {
                    index = index + 1.0;
                    __var5 = index;
                }
                switch(tests.get(__toIndex(__var5))) {
                    case A(String str) -> {
                        {
                            System.out.println(str);
                        }
                    }
                    case Test __var6 -> {
                        if(true) break __var4;
                    }
                    
                }
            }
            
        }
        __ignore(test_function());
    }
    
    public static void main(String[] args) {
        main();
    }
    public static Void test_function() {
        System.out.println("Hello World");
        return null;
    }
    
}
