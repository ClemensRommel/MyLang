import static std.codegen.Functions.*;
import std.codegen.*;

class Main {
    public static class Test {
        public double x;
        
        public double test()  {
            return x;
        }
        public Test() {
            x = 3;
        }
    }
    public static void main() {
        final Test test;
        test = new Test();
        test.x = 4;
        System.out.println(test.test());
    }
    
    public static void main(String[] args) {
        main();
    }
    
}

