import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;

class Main {
    public static double A(double k, __a0 x1, __a0 x2, __a0 x3, __a0 x4, __a0 x5) {
        NumberUpvalue m = new NumberUpvalue();
        m.value = k;
        final Upvalue<__a0>B = new Upvalue<>();
        B.value = () -> {
            m.value = m.value - 1.0;
            return A(m.value, B.value, x1, x2, x3, x4);
        }
        ;
        if((k <= 0.0)) {
            var __a1 = x4.call();
            var __a2 = x5.call();
            return __a1 + __a2;
        } else {
            return B.value.call();
        }
    }
    public static void main() {
        final __a0 __a3 = () -> {
            return 1.0;
        };
        final __a0 __a4 = () -> {
            return -1.0;
        };
        final __a0 __a5 = () -> {
            return -1.0;
        };
        final __a0 __a6 = () -> {
            return 1.0;
        };
        final __a0 __a7 = () -> {
            return 0.0;
        };
        var __a8 = A(10.0, __a3, __a4, __a5, __a6, __a7);
        System.out.println(__a8);
    }
    
    public static void main(String[] args) {
        main();
    }
    @FunctionalInterface static interface __a0 {
        double call();
    }
    
}

