package MyLang;

import java.util.List;
import java.util.Map;

public record MyLangMethod(Object instance, MyLangCallable method) implements MyLangCallable {
    public String getName() {
        return method.getName();
    }

    public Object call(MyLangInterpreter interpreter, List<Object> args, Map<String, Object> named) {
        args.add(0, instance);
        return method.call(interpreter, args, named);
    }

    @Override
    public String toString() {
        return "<method '"+getName()+"'>";
    }
}
