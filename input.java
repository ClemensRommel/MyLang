import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;

class Main {
    public static double test() {
        System.out.println("test");
        return 3.0;
    }
    public static void main() {
        var __a0 = test();
        final double __a1;
        {
            System.out.println("test2");
            __a1 = 4.0;
        }
        System.out.println((__a0 + __a1));
    }
    
    public static void main(String[] args) {
        main();
    }
    
}

