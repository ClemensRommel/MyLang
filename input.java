import static std.codegen.Functions.*;
import std.codegen.*;

class Main {
    public static __a0 error_right_now() {
        NumberUpvalue counter = new NumberUpvalue();counter.value = 0;
        return () -> {
            final double x;x = counter.value;
            counter.value = counter.value + 1;
            return x;
        };
    }
    public static void main() {
        final __a0 f;f = error_right_now();
        System.out.println(f.call());
        System.out.println(f.call());
        System.out.println(f.call());
        System.out.println(f.call());
    }
    
    public static void main(String[] args) {
        main();
    }
    
}
@FunctionalInterface interface __a0 {
    double call();
}

