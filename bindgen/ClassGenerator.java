package bindgen;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassGenerator {
    public static void generate(final String className, final String outputDir) throws ClassNotFoundException {
        final Class<?> targetClass = Class.forName(className);
        final Method[] methods = targetClass.getMethods();
        final Map<Boolean, List<Method>> staticAndInstanceMethods = Arrays.stream(methods).collect(Collectors.partitioningBy(m -> Modifier.isStatic(m.getModifiers())));

        final List<Method> staticMethods = staticAndInstanceMethods.get(true);
        final List<Method> instanceMethods = staticAndInstanceMethods.get(false);

        generateStaticMethods(staticMethods, outputDir, targetClass);
    }

    public static void generateStaticMethods(final List<Method> methods, final String outputDir, final Class<?> c) {
        StringBuilder builder = new StringBuilder();
        var overloaded_sets = methods.stream().collect(Collectors.groupingBy(Method::getName));
        overloaded_sets.forEach((name, list) -> generateOverloadedStaticMethods(name, list, builder));
        System.out.println(builder.toString());
    }

    public static void generateOverloadedStaticMethods(final String name, final List<Method> methods, final StringBuilder builder) {
        var names = NameMangler.generateNames(name, methods);
        names.forEach((mangled_name, method) -> {
            builder.append("native fun "+mangled_name);
            builder.append("(");
            builder.append(Arrays.stream(method.getGenericParameterTypes()).map(TypeMangler::stringify).collect(Collectors.joining(", ")));
            builder.append(") : ");
            builder.append(TypeMangler.stringify(method.getGenericReturnType()));
            builder.append(" := \"");
            builder.append(method.getDeclaringClass().getName()+"."+method.getName());
            builder.append("\";\n");
        });
    }
}
