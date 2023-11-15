package MyLang;

import java.util.List;
import java.util.Map;

public interface MyLangCallable {
    public String getName();
    public String getFileName();
    public Object call(MyLangInterpreter interpreter, List<Object> posArgs, Map<String, Object> namedArgs);

    public default MyLangMethod bind(Object newThis) {
        return new MyLangMethod(newThis, this);
    }
}
