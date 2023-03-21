package MyLang;

import java.util.List;

public record MyLangMethod(Object instance, MyLangCallable method) implements MyLangCallable {
    public String getName() {
        return method.getName();
    }

    public Object call(MyLangInterpreter interpreter, List<Object> args) {
        args.add(0, instance);
        return method.call(interpreter, args);
    }

    @Override
    public String toString() {
        return "<method '"+getName()+"'>";
    }
}
