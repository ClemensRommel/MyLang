import static std.codegen.Functions.*;
import std.codegen.*;

class Main {
    public static __a0 make_var(double init) {
        NumberUpvalue hidden = new NumberUpvalue();
        hidden.value = init;
        final __a1 get = () ->  {
            return hidden.value;
        }
        ;
        final __a2 set = (double x) -> {
            hidden.value = x;
            return null;
        }
        ;
        final __a0 __a3;
        __a3 = new __a0(get, set);
        return __a3;
    }
    public static void main() {
        final __a1 get;
        final __a2 set;
        final __a0 __a4 = make_var(0);
        get = __a4.field0();
        set = __a4.field1();
        System.out.println(get.call());
        ignore(set.call(4));
        System.out.println(get.call());
        ignore(set.call(3));
        System.out.println(get.call());
    }
    
    public static void main(String[] args) {
        main();
    }
    public static class Test {
        public final double smthg;
        {
            smthg = 3;
        }
    }
    
}
@FunctionalInterface interface __a2 {
    Void call(double __a5);
}
@FunctionalInterface interface __a1 {
    double call();
}
record __a0(__a1 field0, __a2 field1) {
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        builder.append(field0);
        builder.append(", ");
        builder.append(field1);
        builder.append(")");
        return builder.toString();
    }
}

