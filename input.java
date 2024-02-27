import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;


public class input {
    public static <A, B> B apply(__generated_input.__var0<A, B> f, A a)  {
        return f.call(a);
    }
    public static void main() {
        final __generated_input.__var0<Double, Boolean> __var1 = (Double n) -> {
            return n > 3.0;
        };
        var __var2 = apply(__var1, 2.0);
        System.out.println(__var2);
    }
    
    public static void main(String[] args) {
        main();
    }
    
}
