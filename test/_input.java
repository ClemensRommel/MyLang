package test;

import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;


public class _input {
    public static void main() {
        final java.lang.StringBuilder strbld;
        strbld = Test();
        __ignore(strbld.append("test"));
        __ignore(strbld.append(" test 2"));
        var __var29 = strbld.toString();
        __ignore(std._implicit.print(__var29));
    }
    
    public static void main(String[] args) {
        __init_runtime(args);
        main();
    }
    public static java.lang.StringBuilder Test() {return new java.lang.StringBuilder();}
    
}
