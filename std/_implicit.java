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
        public default <K> Optional<K> map(mylmyl.__generated_Main.__var60<T, K> f)  {
            switch(this) {
                case None<T>() -> {
                    return new None();
                }
                case Some<T>(T t) -> {
                    var __var61 = f.call(t);
                    return new Some(__var61);
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
                case Some<T>(T __var62) -> {
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
                case Some<T>(T __var63) -> {
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
    public static <T> boolean any(ArrayList<T> list, mylmyl.__generated_Main.__var60<T, Boolean> pred) {
        __var65: for(var __var64 : list) {
            final T t;
            t = __var64;
            {
                var __var66 = pred.call(t);
                if(__var66) {
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
    public static <A, B> B snd(mylmyl.__generated_Main.__var67<A, B> tuple) {
        final A a;
        final B b;
        final mylmyl.__generated_Main.__var67<A, B> __var68 = tuple;
        a = __var68.field0();
        b = __var68.field1();
        return b;
    }
    public static <A, B> A fst(mylmyl.__generated_Main.__var67<A, B> tuple) {
        final A a;
        final B b;
        final mylmyl.__generated_Main.__var67<A, B> __var69 = tuple;
        a = __var69.field0();
        b = __var69.field1();
        return a;
    }
    public static Void print(String __var70) {
        System.out.println(__var70);
        return null;
    }
    public static Void move_line_up() {
        std.BuiltinFunctions.move_line_up();
        return null;
    }
    public static String join(String __var71, ArrayList<String> __var72) {
        return std.BuiltinFunctions.join(__var71, __var72);
    }
    public static ArrayList<String> split(String __var73, String __var74) {
        return std.BuiltinFunctions.split(__var73, __var74);
    }
    public static double len(String __var75) {
        return std.BuiltinFunctions.len(__var75);
    }
    public static int int_len(String __var76) {
        return std.BuiltinFunctions.int_len(__var76);
    }
    public static String strip(String __var77) {
        return std.BuiltinFunctions.strip(__var77);
    }
    public static boolean matches(String __var78, String __var79) {
        return std.BuiltinFunctions.matches(__var78, __var79);
    }
    public static String openFile(String __var80) {
        return std.BuiltinFunctions.openFile(__var80);
    }
    public static boolean isNull(String __var81) {
        return std.BuiltinFunctions.isNull(__var81);
    }
    public static Void writeToFile(String __var82, String __var83) {
        std.BuiltinFunctions.writeToFile(__var82, __var83);
        return null;
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
