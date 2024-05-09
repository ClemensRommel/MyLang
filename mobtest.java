import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;


public class mobtest {
    public static double A(double k, __generated_mobtest.__var0<Double> x1, __generated_mobtest.__var0<Double> x2, __generated_mobtest.__var0<Double> x3, __generated_mobtest.__var0<Double> x4, __generated_mobtest.__var0<Double> x5) {
        NumberUpvalue m = new NumberUpvalue();
        m.value = k;
        final Upvalue<__generated_mobtest.__var0<Double>>B = new Upvalue<>();
        B.value = () -> {
            m.value = m.value - 1.0;
            return A(m.value, B.value, x1, x2, x3, x4);
        }
        ;
        if((k <= 0.0)) {
            var __var1 = x4.call();
            var __var2 = x5.call();
            return __var1 + __var2;
        } else {
            return B.value.call();
        }
    }
    public static void main() {
        final __generated_mobtest.__var0<Double> __var3 = () -> {
            return 1.0;
        };
        final __generated_mobtest.__var0<Double> __var4 = () -> {
            return -1.0;
        };
        final __generated_mobtest.__var0<Double> __var5 = () -> {
            return -1.0;
        };
        final __generated_mobtest.__var0<Double> __var6 = () -> {
            return 1.0;
        };
        final __generated_mobtest.__var0<Double> __var7 = () -> {
            return 0.0;
        };
        var __var8 = A(10.0, __var3, __var4, __var5, __var6, __var7);
        System.out.println(__var8);
    }
    
    public static void main(String[] args) {
        main();
    }
    
}
