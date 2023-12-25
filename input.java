import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;

class Main {
    public static void main() {
        final ArrayList<Double> lst;
        lst = new ArrayList<>(3);
        lst.add(1.0);
        lst.add(2.0);
        lst.add(3.0);
        final ArrayList<Double> results;
        final ArrayList<Double> __a1;
        __a1 = new ArrayList<>();
        for(var __a0 : lst) {
            final double x;
            x = __a0;
            final double __a2;
            {
                __a2 = x;
            }
            __a1.add(__a2);
            
        }
        results = __a1;
        System.out.println(results);
    }
    
    public static void main(String[] args) {
        main();
    }
    
}

