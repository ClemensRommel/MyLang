import static std.codegen.Functions.*;
import std.codegen.*;

class Main {
    public static class Test {
        public double smthg;
        {
            smthg = 0;
        }
        public Void test() {
            System.out.println("Test");
            return null;
        }
    }
    public static void main() {
        final Test test;
        test = new Test();
        ignore(test.test());
    }
    
    public static void main(String[] args) {
        main();
    }
    
}

