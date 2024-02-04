import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;

class Main {
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
        NumberUpvalue index = new NumberUpvalue();
        index.value = -1.0;
        final __var4 get_next_test = () -> {
            index.value = index.value + 1.0;
            return tests.get(__toIndex(index.value));
        }
        ;
        __var5: while(true) {
            {
                var __var6 = get_next_test.call();
                switch(__var6) {
                    case A(String str) -> {
                        {
                            System.out.println(str);
                        }
                    }
                    case Test __var7 -> {
                        if(true) break __var5;
                    }
                }
            }
            
        }
    }
    
    public static void main(String[] args) {
        main();
    }
    @FunctionalInterface static interface __var4 {
        Test call();
    }
    
}

