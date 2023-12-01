import static std.codegen.Functions.*;
import std.codegen.*;

import java.util.ArrayList;

class Main {
    public static void main() {
        final ArrayList<Double> lst;
         lst = new ArrayList<>(3);
        lst.add(1.0);
        lst.add(2.0);
        lst.add(3.0);
        System.out.println(lst.get(__toIndex(1.5)));
    }
    
    public static void main(String[] args) {
        main();
    }
    
}

