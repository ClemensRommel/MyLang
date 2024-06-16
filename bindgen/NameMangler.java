package bindgen;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NameMangler {
    public static Map<String, Method> generateNames(final String name, final List<Method> methods) {
        if(methods.size() == 1) {
            return Map.of(name, methods.getFirst()); // No overloading 
        }

        Map<Integer, List<Method>> methods_by_arity = methods.stream().collect(Collectors.groupingBy(Method::getParameterCount));
        Map<String, Method> names = methods_by_arity.entrySet().stream().flatMap((entry) -> generateNamesForArity(name, entry.getKey(), entry.getValue())).collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
        return names;
    }

    // All methods in methods have arity parameters
    private static Stream<Map.Entry<String, Method>> generateNamesForArity(final String name, final int arity, final List<Method> methods) {
        List<List<Type>> substitutions = new ArrayList<>(); // List of unique differences
        List<Type[]> parameterLists = methods.stream().map(Method::getGenericParameterTypes).toList();
        for(int i = 0; i < arity; i++) {
            int collumn = i;
            var parameters_at_i = parameterLists.stream().map(lst -> lst[collumn]).toList();
            if(parameters_at_i.stream().distinct().count() <= 1) { // All parameters are same -> no difference
                continue;
            }
            if(!substitutions.contains(parameters_at_i)) {
                substitutions.add(parameters_at_i);
            }
        }
        return IntStream.range(0, methods.size())
            .mapToObj(i -> name(i, methods, substitutions));
    }

    private static Map.Entry<String, Method> name(int i, List<Method> methods, List<List<Type>> substitutions) {
        Method method = methods.get(i);
        StringBuilder name = new StringBuilder(method.getName());
        name.append(method.getParameterCount());

        for(List<Type> substitution : substitutions) {
            Type parameterOfMethod = substitution.get(i); // Index into methods list is row in substitutions
            name.append(TypeMangler.stringify(parameterOfMethod));
        }
        return Map.entry(name.toString(), method);
    }
}
