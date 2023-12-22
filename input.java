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
        final __a0 pusher;
        final ArrayList<Double> __a1;
        __a1 = lst;
        pusher = (var a) -> {__a1.add(a); return null;};
        final __a2 popper;
        final ArrayList<Double> __a3;
        __a3 = lst;
        popper = () -> __a3.remove(__a3.size() - 1);
        __ignore(pusher.call(4.0));
        System.out.println(lst);
        var __a4 = popper.call();
        System.out.println(__a4);
        var __a5 = lst.get(lst.size() - 1);
        System.out.println(__a5);
    }
    
    public static void main(String[] args) {
        main();
    }
    @FunctionalInterface static interface __a2 {
        double call();
    }
    @FunctionalInterface static interface __a0 {
        Void call(double __a6);
    }
    
}

