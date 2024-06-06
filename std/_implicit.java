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
        public default <K> Optional<K> map(mylmyl.__generated_Main.__var61<T, K> f)  {
            switch(this) {
                case None<T>() -> {
                    return new None();
                    
                }
                case Some<T>(T t) -> {
                    var __var62 = f.call(t);
                    return new Some(__var62);
                    
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
                case Some<T>(T __var63) -> {
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
                case Some<T>(T __var64) -> {
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
    public static <T> boolean any(ArrayList<T> list, mylmyl.__generated_Main.__var61<T, Boolean> pred) {
        __var66: for(var __var65 : list) {
            final T t;
            t = __var65;
            {
                var __var67 = pred.call(t);
                if(__var67) {
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
    
    public static <T> boolean all(ArrayList<T> list, mylmyl.__generated_Main.__var61<T, Boolean> pred) {
        __var69: for(var __var68 : list) {
            final T t;
            t = __var68;
            {
                var __var70 = pred.call(t);
                if((!__var70)) {
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
    
    public static <A, B> B snd(mylmyl.__generated_Main.__var71<A, B> tuple) {
        final A a;
        final B b;
        final mylmyl.__generated_Main.__var71<A, B> __var72 = tuple;
        a = __var72.field0();
        b = __var72.field1();
        return b;
        
    }
    
    public static <A, B> A fst(mylmyl.__generated_Main.__var71<A, B> tuple) {
        final A a;
        final B b;
        final mylmyl.__generated_Main.__var71<A, B> __var73 = tuple;
        a = __var73.field0();
        b = __var73.field1();
        return a;
        
    }
    
    public static Void print(String __var74) {
        System.out.println(__var74);
        return null;
    }
    public static Void move_line_up() {
        std.BuiltinFunctions.move_line_up();
        return null;
    }
    public static String join(String __var75, ArrayList<String> __var76) {
        return std.BuiltinFunctions.join(__var75, __var76);
    }
    public static ArrayList<String> split(String __var77, String __var78) {
        return std.BuiltinFunctions.split(__var77, __var78);
    }
    public static double len(String __var79) {
        return std.BuiltinFunctions.len(__var79);
    }
    public static int int_len(String __var80) {
        return std.BuiltinFunctions.int_len(__var80);
    }
    public static String strip(String __var81) {
        return std.BuiltinFunctions.strip(__var81);
    }
    public static boolean matches(String __var82, String __var83) {
        return std.BuiltinFunctions.matches(__var82, __var83);
    }
    public static double number(String __var84) {
        return std.BuiltinFunctions.number(__var84);
    }
    public static String replace(String __var85, String __var86, String __var87) {
        return std.BuiltinFunctions.replace(__var85, __var86, __var87);
    }
    public static int int_from_number(double __var88) {
        return std.BuiltinFunctions.int_from_number(__var88);
    }
    
}
