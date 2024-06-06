package test;

import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;


public class Test {

    public static <A> A id(A a) {
        System.out.println(a);
        return a;
    }
}
