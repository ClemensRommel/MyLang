import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;

class Main {
    public static String test() {
        System.out.println("test 2");
        final String fst;
        if(true) return "hello";
        fst = "";
        System.out.println("test 1");
        return fst;
    }
    public static void main() {
        var __a0 = test();
        System.out.println(__a0);
    }
    
    public static void main(String[] args) {
        main();
    }
    
}

