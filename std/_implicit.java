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
        public default <K> Optional<K> map(.__generated_input.__var0<T, K> f)  {
            switch(this) {
                case None<T>() -> {
                    return new None();
                }
                case Some<T>(T t) -> {
                    var __var1 = f.call(t);
                    return new Some(__var1);
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
                case Some<T>(T __var2) -> {
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
                case Some<T>(T __var3) -> {
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
    public static <T> boolean any(ArrayList<T> list, .__generated_input.__var0<T, Boolean> pred) {
        __var5: for(var __var4 : list) {
            final T t;
            t = __var4;
            {
                var __var6 = pred.call(t);
                if(__var6) {
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
    public static <A, B> B snd(.__generated_input.__var7<A, B> tuple) {
        final A a;
        final B b;
        final .__generated_input.__var7<A, B> __var8 = tuple;
        a = __var8.field0();
        b = __var8.field1();
        return b;
    }
    public static <A, B> A fst(.__generated_input.__var7<A, B> tuple) {
        final A a;
        final B b;
        final .__generated_input.__var7<A, B> __var9 = tuple;
        a = __var9.field0();
        b = __var9.field1();
        return a;
    }
    
}
