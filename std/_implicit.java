package std;

import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;


public class _implicit {
    public static  sealed interface Optional<T> {
        public default T unwrap()  {
            switch(this) {
                case Some<T>(T t) -> {
                    return t;
                    
                }
                case None<T>() -> {
                    return panic("Error: called unwrap on empty Optional");
                    
                }
                default -> { panic("Inexhaustive match: "+this); throw new RuntimeException();}
                
            }
            
        }
        public default <K> Optional<K> map(mylmyl.__generated_Main.__var62<T, K> f)  {
            switch(this) {
                case None<T>() -> {
                    return new None();
                    
                }
                case Some<T>(T t) -> {
                    var __var63 = f.call(t);
                    return new Some(__var63);
                    
                }
                default -> { panic("Inexhaustive match: "+this); throw new RuntimeException();}
                
            }
            
        }
        public default T expect(String e)  {
            switch(this) {
                case Some<T>(T t) -> {
                    return t;
                    
                }
                case None<T>() -> {
                    return panic(e);
                    
                }
                default -> { panic("Inexhaustive match: "+this); throw new RuntimeException();}
                
            }
            
        }
        public default boolean is_some()  {
            switch(this) {
                case Some<T>(T __var64) -> {
                    return true;
                    
                }
                case None<T>() -> {
                    return false;
                    
                }
                default -> { panic("Inexhaustive match: "+this); throw new RuntimeException();}
                
            }
            
        }
        public default boolean is_none()  {
            switch(this) {
                case Some<T>(T __var65) -> {
                    return false;
                    
                }
                case None<T>() -> {
                    return true;
                    
                }
                default -> { panic("Inexhaustive match: "+this); throw new RuntimeException();}
                
            }
            
        }
        
    }
    public static  record None<T>() implements Optional<T> {
        @Override public String toString() {
            StringBuilder __builder = new StringBuilder();
            __builder.append("None(");
            __builder.append(")");
            return __builder.toString();
        } 
    }
    public static  record Some<T>(T __field0) implements Optional<T> {
        @Override public String toString() {
            StringBuilder __builder = new StringBuilder();
            __builder.append("Some(");
            __builder.append(__field0());
            __builder.append(")");
            return __builder.toString();
        } 
    }
    public static <T> boolean any(ArrayList<T> list, mylmyl.__generated_Main.__var62<T, Boolean> pred) {
        __var67: for(var __var66 : list) {
            final T t;
            t = __var66;
            {
                var __var68 = pred.call(t);
                if(__var68) {
                    {
                        if(true) {
                            return true;
                            
                        }
                    }
                    
                }
            }
            
        }
        return false;
        
    }
    
    public static <T> boolean all(ArrayList<T> list, mylmyl.__generated_Main.__var62<T, Boolean> pred) {
        __var70: for(var __var69 : list) {
            final T t;
            t = __var69;
            {
                var __var71 = pred.call(t);
                if((!__var71)) {
                    {
                        if(true) {
                            return false;
                            
                        }
                    }
                    
                }
            }
            
        }
        return true;
        
    }
    
    public static <A, B> B snd(mylmyl.__generated_Main.__var72<A, B> tuple) {
        final A a;
        final B b;
        final mylmyl.__generated_Main.__var72<A, B> __var73 = tuple;
        a = __var73.field0();
        b = __var73.field1();
        return b;
        
    }
    
    public static <A, B> A fst(mylmyl.__generated_Main.__var72<A, B> tuple) {
        final A a;
        final B b;
        final mylmyl.__generated_Main.__var72<A, B> __var74 = tuple;
        a = __var74.field0();
        b = __var74.field1();
        return a;
        
    }
    
    public static Void print(String __var75) {
        System.out.println(__var75);
        return null;
    }
    public static Void move_line_up() {
        std.BuiltinFunctions.move_line_up();
        return null;
    }
    public static String join(String __var76, ArrayList<String> __var77) {
        return std.BuiltinFunctions.join(__var76, __var77);
    }
    public static ArrayList<String> split(String __var78, String __var79) {
        return std.BuiltinFunctions.split(__var78, __var79);
    }
    public static double len(String __var80) {
        return std.BuiltinFunctions.len(__var80);
    }
    public static int int_len(String __var81) {
        return std.BuiltinFunctions.int_len(__var81);
    }
    public static String strip(String __var82) {
        return std.BuiltinFunctions.strip(__var82);
    }
    public static boolean matches(String __var83, String __var84) {
        return std.BuiltinFunctions.matches(__var83, __var84);
    }
    public static double number(String __var85) {
        return std.BuiltinFunctions.number(__var85);
    }
    public static String replace(String __var86, String __var87, String __var88) {
        return std.BuiltinFunctions.replace(__var86, __var87, __var88);
    }
    public static int int_from_number(double __var89) {
        return std.BuiltinFunctions.int_from_number(__var89);
    }
    
}
