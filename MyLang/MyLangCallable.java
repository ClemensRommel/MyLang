package MyLang;

import java.util.List;

public interface MyLangCallable {
    public String getName();
    public Object call(MyLangInterpreter interpreter, List<Object> args);

    public default MyLangMethod bind(Object newThis) {
        return new MyLangMethod(newThis, this);
    }
}
