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
        public default <K> Optional<K> map(mylmyl.__generated_Main.__var58<T, K> f)  {
            switch(this) {
                case None<T>() -> {
                    return new None();
                }
                case Some<T>(T t) -> {
                    var __var59 = f.call(t);
                    return new Some(__var59);
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
                case Some<T>(T __var60) -> {
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
                case Some<T>(T __var61) -> {
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
    public static <T> boolean any(ArrayList<T> list, mylmyl.__generated_Main.__var58<T, Boolean> pred) {
        __var63: for(var __var62 : list) {
            final T t;
            t = __var62;
            {
                var __var64 = pred.call(t);
                if(__var64) {
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
    public static <A, B> B snd(mylmyl.__generated_Main.__var65<A, B> tuple) {
        final A a;
        final B b;
        final mylmyl.__generated_Main.__var65<A, B> __var66 = tuple;
        a = __var66.field0();
        b = __var66.field1();
        return b;
    }
    public static <A, B> A fst(mylmyl.__generated_Main.__var65<A, B> tuple) {
        final A a;
        final B b;
        final mylmyl.__generated_Main.__var65<A, B> __var67 = tuple;
        a = __var67.field0();
        b = __var67.field1();
        return a;
    }
    public static Void print(String __var68) {
        System.out.println(__var68);
        return null;
    }
    public static Void move_line_up() {
        std.BuiltinFunctions.move_line_up();
        return null;
    }
    public static String join(String __var69, ArrayList<String> __var70) {
        return std.BuiltinFunctions.join(__var69, __var70);
    }
    public static ArrayList<String> split(String __var71, String __var72) {
        return std.BuiltinFunctions.split(__var71, __var72);
    }
    public static double len(String __var73) {
        return std.BuiltinFunctions.len(__var73);
    }
    public static String strip(String __var74) {
        return std.BuiltinFunctions.strip(__var74);
    }
    public static boolean matches(String __var75, String __var76) {
        return std.BuiltinFunctions.matches(__var75, __var76);
    }
    public static String openFile(String __var77) {
        return std.BuiltinFunctions.openFile(__var77);
    }
    public static boolean isNull(String __var78) {
        return std.BuiltinFunctions.isNull(__var78);
    }
    public static Void writeToFile(String __var79, String __var80) {
        std.BuiltinFunctions.writeToFile(__var79, __var80);
        return null;
    }
    public static double number(String __var81) {
        return std.BuiltinFunctions.number(__var81);
    }
    public static String replace(String __var82, String __var83, String __var84) {
        return std.BuiltinFunctions.replace(__var82, __var83, __var84);
    }
    
}
