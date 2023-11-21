import static std.codegen.Functions.*;
import std.codegen.*;

class Main {
    public static double A(double k, __a0 x1, __a0 x2, __a0 x3, __a0 x4, __a0 x5) {
        NumberUpvalue m = new NumberUpvalue();
        m.value = k;
        final Upvalue<__a0> B = new Upvalue<>();
        B.value = () -> {
            m.value = m.value - 1;
            return A(m.value, B.value, x1, x2, x3, x4);
        }
        ;
        if((k <= 0)) {
            return x4.call() + x5.call();
        } else {
            return B.value.call();
        }
    }
    public static void main() {
        final __a0 __a1 = () -> {
            return 1;
        };
        final __a0 __a2 = () -> {
            return -1;
        };
        final __a0 __a3 = () -> {
            return -1;
        };
        final __a0 __a4 = () -> {
            return 1;
        };
        final __a0 __a5 = () -> {
            return 0;
        };
        System.out.println(A(10, __a1, __a2, __a3, __a4, __a5));
    }
    
    public static void main(String[] args) {
        main();
    }
    
}
@FunctionalInterface interface __a0 {
    double call();
}

