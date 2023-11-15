package MyLang;

import java.util.List;
import java.util.Map;

public record MyLangMethod(Object instance, MyLangCallable method) implements MyLangCallable {
    public String getName() {
        if(instance instanceof MyLangObject o) {
            return o.klass.name()+"."+method.getName();
        }
        return method.getName();
    }
    public String getFileName() {
        return method.getFileName();
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
