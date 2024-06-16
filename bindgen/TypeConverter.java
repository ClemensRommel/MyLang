package bindgen;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import java.util.ArrayList;
import java.util.HashMap;

public class TypeConverter {
    public static String convert(Type t) {
        switch(t) {
            case Class<?> c -> {
                return TypeMangler.stringify(t);
            }
            case GenericArrayType g -> {
                if(g.getGenericComponentType().equals(float.class)) {
                    return "FloatArray";
                }
                if(g.getGenericComponentType().equals(double.class)) {
                    return "NumberArray";
                }
                if(g.getGenericComponentType().equals(long.class)) {
                    return "LongArray";
                }
                if(g.getGenericComponentType().equals(int.class)) {
                    return "IntArray";
                }
                if(g.getGenericComponentType().equals(short.class)) {
                    return "ShortArray";
                }
                if(g.getGenericComponentType().equals(byte.class)) {
                    return "ByteArray";
                }
                throw new RuntimeException("Cannot convert array type: "+t);
            }
            case TypeVariable<?> tv -> {
                return tv.getName();
            }
            case WildcardType w -> {
                throw new RuntimeException("Cannot convert wildcard type: "+t);
            }
            case ParameterizedType p -> {
                if(p.getRawType().equals(ArrayList.class)) {
                    return convert(p.getActualTypeArguments()[0]) + "[]"; // Special case for builtin list type
                }
                if(p.getRawType().equals(HashMap.class)) {
                    return "std.collections.HashMap("+convert(p.getActualTypeArguments()[0])+","+convert(p.getActualTypeArguments()[1])+")";
                }
                throw new RuntimeException("Cannot convert parameterized type: "+t);
            }
            default -> {
                throw new RuntimeException("Inexhaustive match: "+t);
            }
        }
    }
}
