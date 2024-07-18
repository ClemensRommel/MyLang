package test_files;

import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;


public class _mobtest {
    public static double A(double k, test_files.__generated_mobtest.__var28<Double> x1, test_files.__generated_mobtest.__var28<Double> x2, test_files.__generated_mobtest.__var28<Double> x3, test_files.__generated_mobtest.__var28<Double> x4, test_files.__generated_mobtest.__var28<Double> x5) {
        NumberUpvalue m = new NumberUpvalue();
        m.value = k;
        final Upvalue<test_files.__generated_mobtest.__var28<Double>>B = new Upvalue<>();
        B.value = () -> {
            m.value = m.value-((double)1.0);
            return A(m.value, B.value, x1, x2, x3, x4);
            
        }
        
        ;
        if((k<=((double)0.0))) {
            var __var29 = x4.call();
            var __var30 = x5.call();
            return __var29+__var30;
            
        } else {
            return B.value.call();
            
        }
        
    }
    
    public static void main() {
        final test_files.__generated_mobtest.__var28<Double> __var31 = () -> {
            return ((double)1.0);
            
        };
        final test_files.__generated_mobtest.__var28<Double> __var32 = () -> {
            return -((double)1.0);
            
        };
        final test_files.__generated_mobtest.__var28<Double> __var33 = () -> {
            return -((double)1.0);
            
        };
        final test_files.__generated_mobtest.__var28<Double> __var34 = () -> {
            return ((double)1.0);
            
        };
        final test_files.__generated_mobtest.__var28<Double> __var35 = () -> {
            return ((double)0.0);
            
        };
        var __var36 = A(((double)10.0), __var31, __var32, __var33, __var34, __var35);
        std._implicit.print((__stringify("")+__stringify(__var36)));
        
    }
    
    
    public static void main(String[] args) {
        __init_runtime(args);
        main();
    }
    
}
