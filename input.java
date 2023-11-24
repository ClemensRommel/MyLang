import static std.codegen.Functions.*;
import std.codegen.*;

import java.util.ArrayList;

class Main {
    public static void main() {
        final ArrayList<Double> test;
         test = new ArrayList<>(3);
        test.add(1.0);
        test.add(2.0);
        test.add(3.0);
        System.out.println(test);
    }
    
    public static void main(String[] args) {
        main();
    }
    
}

