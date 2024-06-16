package bindgen;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;

public class TypeMangler {
    public static String stringify(Type type) {
        switch(type) {
            case Class<?> c -> {
                return potentialPrimitive(c);
            }
            case GenericArrayType g -> {
                return "Array"+stringify(g.getGenericComponentType());
            }
            case TypeVariable<?> t -> {
                return t.getName();
            }
            case WildcardType w -> {
                return "Unknown";
            }
            case ParameterizedType p -> {
                if(p.getRawType().equals(ArrayList.class)) {
                    return "List"+stringify(p.getActualTypeArguments()[0]); // Special case for builtin list type
                }
                StringBuilder builder = new StringBuilder(stringify(p.getRawType()));
                Type[] args = p.getActualTypeArguments();
                builder.append(args.length);
                for(Type arg : args) {
                    builder.append(stringify(arg));
                }
                return builder.toString();
            }
            default -> {
                throw new RuntimeException("Inexhaustive match: "+type);
            }
        }
    }
    private static String potentialPrimitive(Class<?> c) {
        if(c.equals(void.class) || c.equals(Void.class)) {
            return "Void";
        }
        if(c.equals(boolean.class) || c.equals(Boolean.class)) {
            return "Bool";
        }
        if(c.equals(double.class) || c.equals(Double.class)) {
            return "Number";
        }
        if(c.equals(float.class) || c.equals(Float.class)) {
            return "Float";
        }
        if(c.equals(long.class) || c.equals(Long.class)) {
            return "Long";
        }
        if(c.equals(int.class) || c.equals(Integer.class)) {
            return "Int";
        }
        if(c.equals(short.class) || c.equals(Short.class)) {
            return "Short";
        }
        if(c.equals(byte.class) || c.equals(Byte.class)) {
            return "Byte";
        }
        if(c.equals(String.class)) {
            return "String";
        }
        if(c.isArray()) {
            var elem_ty = c.getComponentType();
            if(elem_ty.equals(double.class)) {
                return "NumberArray";
            }
            if(elem_ty.equals(float.class)) {
                return "FloatArray";
            }
            if(elem_ty.equals(long.class)) {
                return "LongArray";
            }
            if(elem_ty.equals(int.class)) {
                return "IntArray";
            }
            if(elem_ty.equals(short.class)) {
                return "ShortArray";
            }
            if(elem_ty.equals(byte.class)) {
                return "ByteArray";
            }
            throw new RuntimeException("Cannot convert array type: "+c);
        }
        return c.getSimpleName();
    }
}
