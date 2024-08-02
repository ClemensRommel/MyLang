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
        public default <K> Optional<K> map(mylmyl.__generated_Main.__var64<T, K> f)  {
            switch(this) {
                case None<T>() -> {
                    return new None();
                    
                }
                case Some<T>(T t) -> {
                    var __var65 = f.call(t);
                    return new Some(__var65);
                    
                }
                default -> { panic("Inexhaustive match: "+this); throw new RuntimeException();}
                
            }
            
        }
        public default <K> Optional<K> flatmap(mylmyl.__generated_Main.__var64<T, Optional<K>> f)  {
            switch(this) {
                case None<T>() -> {
                    return new None();
                    
                }
                case Some<T>(T t) -> {
                    return f.call(t);
                    
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
                case Some<T>(T __var66) -> {
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
                case Some<T>(T __var67) -> {
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
    public static <T> boolean any(ArrayList<T> list, mylmyl.__generated_Main.__var64<T, Boolean> pred) {
        __var69: for(var __var68 : list) {
            final T t;
            t = __var68;
            {
                var __var70 = pred.call(t);
                if(__var70) {
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
    
    public static <T> boolean all(ArrayList<T> list, mylmyl.__generated_Main.__var64<T, Boolean> pred) {
        __var72: for(var __var71 : list) {
            final T t;
            t = __var71;
            {
                var __var73 = pred.call(t);
                if((!__var73)) {
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
    
    public static <A, B> B snd(mylmyl.__generated_Main.__var74<A, B> tuple) {
        final A a;
        final B b;
        final mylmyl.__generated_Main.__var74<A, B> __var75 = tuple;
        a = __var75.field0();
        b = __var75.field1();
        return b;
        
    }
    
    public static <A, B> A fst(mylmyl.__generated_Main.__var74<A, B> tuple) {
        final A a;
        final B b;
        final mylmyl.__generated_Main.__var74<A, B> __var76 = tuple;
        a = __var76.field0();
        b = __var76.field1();
        return a;
        
    }
    
    public static Void print(String __var77) {
        System.out.println(__var77);
        return null;
    }
    public static Void move_line_up() {
        std.BuiltinFunctions.move_line_up();
        return null;
    }
    public static String join(String __var78, ArrayList<String> __var79) {
        return std.BuiltinFunctions.join(__var78, __var79);
    }
    public static ArrayList<String> split(String __var80, String __var81) {
        return std.BuiltinFunctions.split(__var80, __var81);
    }
    public static double len(String __var82) {
        return std.BuiltinFunctions.len(__var82);
    }
    public static int int_len(String __var83) {
        return std.BuiltinFunctions.int_len(__var83);
    }
    public static String strip(String __var84) {
        return std.BuiltinFunctions.strip(__var84);
    }
    public static boolean matches(String __var85, String __var86) {
        return std.BuiltinFunctions.matches(__var85, __var86);
    }
    public static double number(String __var87) {
        return std.BuiltinFunctions.number(__var87);
    }
    public static String replace(String __var88, String __var89, String __var90) {
        return std.BuiltinFunctions.replace(__var88, __var89, __var90);
    }
    
}
