package std;

import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;

import mylmyl.__generated_Main;

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
        public default <K> Optional<K> map(mylmyl.__generated_Main.__var52<T, K> f)  {
            switch(this) {
                case None<T>() -> {
                    return new None();
                }
                case Some<T>(T t) -> {
                    var __var53 = f.call(t);
                    return new Some(__var53);
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
                case Some<T>(T __var54) -> {
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
                case Some<T>(T __var55) -> {
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
    public static <T> boolean any(ArrayList<T> list, mylmyl.__generated_Main.__var52<T, Boolean> pred) {
        __var57: for(var __var56 : list) {
            final T t;
            t = __var56;
            {
                var __var58 = pred.call(t);
                if(__var58) {
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
    public static <A, B> B snd(mylmyl.__generated_Main.__var59<A, B> tuple) {
        final A a;
        final B b;
        final mylmyl.__generated_Main.__var59<A, B> __var60 = tuple;
        a = __var60.field0();
        b = __var60.field1();
        return b;
    }
    public static <A, B> A fst(mylmyl.__generated_Main.__var59<A, B> tuple) {
        final A a;
        final B b;
        final mylmyl.__generated_Main.__var59<A, B> __var61 = tuple;
        a = __var61.field0();
        b = __var61.field1();
        return a;
    }
    
}
